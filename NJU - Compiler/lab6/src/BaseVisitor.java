import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.*;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.util.ArrayList;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class BaseVisitor extends SysYParserBaseVisitor<LLVMValueRef>{
    Scope currentScope;

    int varCnt, blockCnt, conditionVarCnt;

    LLVMModuleRef module;
    LLVMBuilderRef builder;
    LLVMTypeRef i32Type, i1Type;
    LLVMValueRef zero, one;

    LLVMValueRef currentFunction;
    LLVMBasicBlockRef currentFunctionEntry;

    int loopCnt = 0;
    ArrayList<LLVMBasicBlockRef> loopConditions;
    ArrayList<LLVMBasicBlockRef> loopExits;

    HashMap<LLVMBasicBlockRef, Integer> Blockbr;

    public BaseVisitor() {
        varCnt = blockCnt = conditionVarCnt = 0;

        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget(); 

        module = LLVMModuleCreateWithName("module");
        builder = LLVMCreateBuilder();
        i32Type = LLVMInt32Type();
        i1Type = LLVMInt1Type();
        zero = LLVMConstInt(i32Type, 0, 0);
        one = LLVMConstInt(i32Type, 1, 0);
        currentFunction = null;
        currentFunctionEntry = null;

        loopConditions = new ArrayList<LLVMBasicBlockRef>();
        loopExits = new ArrayList<LLVMBasicBlockRef>();
        loopCnt = 0;
        Blockbr = new HashMap<LLVMBasicBlockRef, Integer>();
    }

    @Override public LLVMValueRef visitProg(SysYParser.ProgContext ctx) { 

        currentScope = new Scope("GlobalScope", null);
        LLVMValueRef tmp = visitChildren(ctx); 
        LLVMDumpModule(module);
        return tmp;
    }

    @Override public LLVMValueRef visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        int type = token.getType();
        String text = token.getText();
        int idx = token.getTokenIndex();

        if (type >= 1 && type <= 9) { // key word : Bright Cyan
        } else if (type >= 10 && type <= 28) { // operator : Bright Red
        } else if (type == 29) { // const int : Magenta
        } else if (type == 37 || type == 35 || type == 39) {
        } else if (type == 38 || type == 36 || type == 40) {
        }

        if (type == 37) {  // {
            currentScope = new Scope("block", currentScope);
        } else if (type == 38) { // }
            currentScope = currentScope.getEnclosScope();
        } else if (type == 8) { // else
        }

        LLVMValueRef tmp = new LLVMValueRef();
        return tmp;
    }

    @Override public LLVMValueRef visitBlock(SysYParser.BlockContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }
        LLVMValueRef tmp = visitChildren(ctx);
        
        return tmp;
    }

    @Override public LLVMValueRef visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }

        LLVMTypeRef returnType = i32Type;
        // PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);
            
        PointerPointer<Pointer> argumentTypes;
        int paraCnt = 0;
        if (ctx.funcDecl().parameters() != null) {
            paraCnt = ctx.funcDecl().parameters().parameter().size();
            argumentTypes = new PointerPointer<>(paraCnt);
            for (int i = 0; i < paraCnt; i++) {
                argumentTypes.put(i, i32Type);
            }
        } else {
            argumentTypes = new PointerPointer<>(0);
        }

        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, paraCnt, 0);
        LLVMValueRef function = LLVMAddFunction(module, ctx.funcDecl().IDENT().getText(), ft);
        currentScope.define(ctx.funcDecl().IDENT().getText(), function);
        currentFunction = function;
        
        // New Scope
        currentScope = new Scope("functionScope", currentScope);
        
        String str = ctx.funcDecl().IDENT().getText() + "Entry";
        LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, str);
        LLVMPositionBuilderAtEnd(builder, block1);
        
        currentFunctionEntry = block1;

        for (int i = 0 ; i < paraCnt; i++) {
            LLVMValueRef x = LLVMGetParam(function, i);
            String varName = ((SysYParser.ParameterIntContext)ctx.funcDecl().parameters().parameter(i)).IDENT().getText();
            
            if (currentScope.countCurrentScope(varName) != null) {
                continue;
            }

            LLVMValueRef var = LLVMBuildAlloca(builder, i32Type, varName);
            LLVMBuildStore(builder, x, var);
            
            currentScope.define(varName, var);
        }

        LLVMValueRef tmp = visitChildren(ctx);

        currentScope = currentScope.getEnclosScope();
        currentFunction = null;
        currentFunctionEntry = null;

        return tmp;
    }

    @Override public LLVMValueRef visitDecimal(SysYParser.DecimalContext ctx) {
        visitChildren(ctx);

        String str = ctx.getText();
        int len = str.length();
        int res = 0;
        for (int i = 0; i < len; i++) {
            res = res * 10 + (str.charAt(i) - 48);
        }
        LLVMValueRef t = LLVMConstInt(i32Type, res, 0);
        LLVMValueRef result = LLVMBuildAdd(builder, t, zero, /* varName:String */"result");

        return result;
    }
	@Override public LLVMValueRef visitBinary(SysYParser.BinaryContext ctx) {
        visitChildren(ctx);

        String str = ctx.getText();
        int len = str.length();
        int res = 0;
        for (int i = 2; i < len; i++) {
            res = (res << 1) | (str.charAt(i) - 48);
        }
        LLVMValueRef t = LLVMConstInt(i32Type, res, 0);
        LLVMValueRef result = LLVMBuildAdd(builder, t, zero, /* varName:String */"result");

        return result;
    }
	@Override public LLVMValueRef visitOctal(SysYParser.OctalContext ctx) {
        visitChildren(ctx);

        String str = ctx.getText();
        int len = str.length();
        int res = 0;
        for (int i = 1; i < len; i++) {
            res = (res << 3) + (str.charAt(i) - 48);
        }
        LLVMValueRef t = LLVMConstInt(i32Type, res, 0);
        LLVMValueRef result = LLVMBuildAdd(builder, t, zero, /* varName:String */"result");

        return result;
    }
	@Override public LLVMValueRef visitHexadecimal(SysYParser.HexadecimalContext ctx) {
        visitChildren(ctx);

        String str = ctx.getText();
        int len = str.length();
        int res = 0;
        for (int i = 2; i < len; i++) {
            char ch = str.charAt(i);
            if (ch >= '0' && ch <= '9') {
                res = (res << 4) + (ch - 48);
            } else if (ch >= 'a' && ch <= 'f') {
                res = (res << 4) + (ch - 97 + 10);
            } else {
                res = (res << 4) + (ch - 65 + 10);
            }
        }
        LLVMValueRef t = LLVMConstInt(i32Type, res, 0);
        LLVMValueRef result = LLVMBuildAdd(builder, t, zero, /* varName:String */"result");

        return result;
    }

    @Override public LLVMValueRef visitParameters(SysYParser.ParametersContext ctx) {
        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }
    
    @Override public LLVMValueRef visitFunctionCall(SysYParser.FunctionCallContext ctx) {
        visit(ctx.IDENT());

        String funcName = ctx.IDENT().getText();
        LLVMValueRef function = currentScope.resolve(funcName);

        int paraCnt = 0;
        if (ctx.funcRParams() != null) {
            paraCnt = ctx.funcRParams().param().size();
        }

        PointerPointer<LLVMValueRef> paras = new PointerPointer<>(paraCnt);
        for (int i = 0; i < paraCnt; i++) {
            LLVMValueRef x = visit(ctx.funcRParams().param(i).exp());
            paras.put(i, x);
        }

        LLVMTypeRef funcType = LLVMGetElementType(LLVMTypeOf(function));
        return LLVMBuildCall2(builder, funcType, function, paras, paraCnt, funcName);
    }

	@Override public LLVMValueRef visitVariableDecl(SysYParser.VariableDeclContext ctx) {    
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }

        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }

    @Override public LLVMValueRef visitExpToCond(SysYParser.ExpToCondContext ctx) {
        LLVMValueRef exp = visit(ctx.exp());
        return exp;
    }
    @Override public LLVMValueRef visitLessThan(SysYParser.LessThanContext ctx) {
        LLVMValueRef left = visit(ctx.cond(0));
        int opType = 0;
        if (ctx.LT() != null) {
            opType = 1;
        } else if (ctx.GT() != null) {
            opType = 2;   
        } else if (ctx.LE() != null) {
            opType = 3;   
        } else {
            opType = 4;
        }
        LLVMValueRef right = visit(ctx.cond(1));

        LLVMValueRef result;
        if (opType == 1) {
            result = LLVMBuildICmp(builder, LLVMIntSLT, left, right, "result");
        } else if (opType == 2) {
            result = LLVMBuildICmp(builder, LLVMIntSGT, left, right, "result");
        } else if (opType == 3) {
            result = LLVMBuildICmp(builder, LLVMIntSLE, left, right, "result");
        } else {
            result = LLVMBuildICmp(builder, LLVMIntSGE, left, right, "result");
        }
        result = LLVMBuildZExt(builder, result, i32Type, "result");
        return result;
    }
    @Override public LLVMValueRef visitEqual(SysYParser.EqualContext ctx) {
        LLVMValueRef left = visit(ctx.cond(0));
        int opType = 0;
        if (ctx.EQ() != null) {
            opType = 1;
        } else {
            opType = 2;
        }
        LLVMValueRef right = visit(ctx.cond(1));

        LLVMValueRef result;
        if (opType == 1) {
            result = LLVMBuildICmp(builder, LLVMIntEQ, left, right, "result");
        } else {
            result = LLVMBuildICmp(builder, LLVMIntNE, left, right, "result");
        }
        result = LLVMBuildZExt(builder, result, i32Type, "result");
        return result;
    }
    @Override public LLVMValueRef visitLogicalAnd(SysYParser.LogicalAndContext ctx) {
        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
            
        LLVMValueRef terminator = LLVMGetBasicBlockTerminator(currentFunctionEntry);
        if (terminator != null) {
            LLVMPositionBuilderBefore(builder, terminator);
        } else {
            LLVMPositionBuilderAtEnd(builder, currentFunctionEntry);
        }
        LLVMValueRef condResultPos = LLVMBuildAlloca(builder, i32Type, "condResult");
        LLVMPositionBuilderAtEnd(builder, currentBlock);

        LLVMValueRef left = visit(ctx.cond(0));
        LLVMValueRef i1Left = LLVMBuildICmp(builder, LLVMIntNE, left, LLVMConstInt(i32Type, 0, 0), newConditionVar());
        LLVMBuildStore(builder, left, condResultPos);

        LLVMBasicBlockRef lTrue = LLVMAppendBasicBlock(currentFunction, newBlock("lTrue"));
        LLVMBasicBlockRef lFalse = LLVMAppendBasicBlock(currentFunction, newBlock("lFalse"));

        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildCondBr(builder, i1Left, lTrue, lFalse);
        }

        LLVMPositionBuilderAtEnd(builder, lTrue);
        LLVMValueRef right = visit(ctx.cond(1));
        LLVMValueRef result = LLVMBuildAnd(builder, left, right, "result");
        LLVMBuildStore(builder, result, condResultPos);

        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildBr(builder, lFalse);
        }

        LLVMPositionBuilderAtEnd(builder, lFalse);
        LLVMValueRef condResult = LLVMBuildLoad(builder, condResultPos, "condResult");
        
        return condResult;
    }
    @Override public LLVMValueRef visitLogicalOr(SysYParser.LogicalOrContext ctx) {
        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
            
        LLVMValueRef terminator = LLVMGetBasicBlockTerminator(currentFunctionEntry);
        if (terminator != null) {
            LLVMPositionBuilderBefore(builder, terminator);
        } else {
            LLVMPositionBuilderAtEnd(builder, currentFunctionEntry);
        }
        LLVMValueRef condResultPos = LLVMBuildAlloca(builder, i32Type, "condResult");
        LLVMPositionBuilderAtEnd(builder, currentBlock);

        LLVMValueRef left = visit(ctx.cond(0));
        LLVMValueRef i1Left = LLVMBuildICmp(builder, LLVMIntNE, left, LLVMConstInt(i32Type, 0, 0), newConditionVar());
        LLVMBuildStore(builder, left, condResultPos);

        LLVMBasicBlockRef lFalse = LLVMAppendBasicBlock(currentFunction, newBlock("lFalse"));
        LLVMBasicBlockRef lTrue = LLVMAppendBasicBlock(currentFunction, newBlock("lTrue"));

        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildCondBr(builder, i1Left, lTrue, lFalse);
        }

        LLVMPositionBuilderAtEnd(builder, lFalse);
        LLVMValueRef right = visit(ctx.cond(1));
        LLVMValueRef result = LLVMBuildOr(builder, left, right, "result");
        LLVMBuildStore(builder, result, condResultPos);

        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildBr(builder, lTrue);
        }

        LLVMPositionBuilderAtEnd(builder, lTrue);
        LLVMValueRef condResult = LLVMBuildLoad(builder, condResultPos, "condResult");
        
        return condResult;
    }

    @Override public LLVMValueRef visitContinue(SysYParser.ContinueContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }

        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
        Blockbr.put(currentBlock, 1);
        LLVMBuildBr(builder, loopConditions.get(loopCnt - 1));
        return null;
    }
	@Override public LLVMValueRef visitBreak(SysYParser.BreakContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }

        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
        Blockbr.put(currentBlock, 1);
        LLVMBuildBr(builder, loopExits.get(loopCnt - 1));
        return null;
    }


    @Override public LLVMValueRef visitWhileLoop(SysYParser.WhileLoopContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }
        // LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);

        LLVMBasicBlockRef conditionCheck = LLVMAppendBasicBlock(currentFunction, newBlock("WhileCondition"));
        LLVMBasicBlockRef block = LLVMAppendBasicBlock(currentFunction, newBlock("WhileLoop"));
        LLVMBasicBlockRef exit = LLVMAppendBasicBlock(currentFunction, newBlock("WhileExit"));
        ++loopCnt;
        loopConditions.add(conditionCheck);
        loopExits.add(exit);
        
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildBr(builder, conditionCheck);  
        }

        LLVMPositionBuilderAtEnd(builder, conditionCheck);
        LLVMValueRef cond = visit(ctx.cond());
        cond = LLVMBuildICmp(builder, LLVMIntNE, cond, LLVMConstInt(i32Type, 0, 0), newConditionVar());
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildCondBr(builder, cond, block, exit);
        }

        LLVMPositionBuilderAtEnd(builder, block);
        visit(ctx.stat());
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
            Blockbr.put(LLVMGetInsertBlock(builder), 1);
            LLVMBuildBr(builder, conditionCheck);
        }

        LLVMPositionBuilderAtEnd(builder, exit);

        --loopCnt;
        loopConditions.remove(loopCnt);
        loopExits.remove(loopCnt);
        
        return null;
    }

    @Override public LLVMValueRef visitIfElse(SysYParser.IfElseContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }

        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
        if (ctx.ELSE() != null) {
            LLVMBasicBlockRef ifBlock = LLVMAppendBasicBlock(currentFunction, newBlock("IfTrue"));
            LLVMBasicBlockRef elseBlock = LLVMAppendBasicBlock(currentFunction, newBlock("IfFalse"));
            LLVMBasicBlockRef ifExit = LLVMAppendBasicBlock(currentFunction, newBlock("IfExit"));
            
            LLVMValueRef cond = visit(ctx.cond());
            cond = LLVMBuildICmp(builder, LLVMIntNE, cond, LLVMConstInt(i32Type, 0, 0), newConditionVar());
            // if (true) ifBlock else elseBlock;
            if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
                Blockbr.put(LLVMGetInsertBlock(builder), 1);
                LLVMBuildCondBr(builder, cond, ifBlock, elseBlock);
            }
            
            LLVMPositionBuilderAtEnd(builder, ifBlock);
            visit(ctx.stat(0));
            if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
                Blockbr.put(LLVMGetInsertBlock(builder), 1);
                LLVMBuildBr(builder, ifExit);
            }
            // LLVMBasicBlockRef finishIfBlock = LLVMGetInsertBlock(builder);
            
            // LLVMPositionBuilderAtEnd(builder, ifBlock);
            // br ifExit
            
            LLVMPositionBuilderAtEnd(builder, elseBlock);
            visit(ctx.stat(1));
            if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
                Blockbr.put(LLVMGetInsertBlock(builder), 1);
                LLVMBuildBr(builder, ifExit);
            }
            // LLVMBasicBlockRef finishElseBlock = LLVMGetInsertBlock(builder);
            // LLVMPositionBuilderAtEnd(builder, elseBlock);
            // br ifExit
            // LLVMBuildBr(builder, ifExit);
            

            LLVMPositionBuilderAtEnd(builder, ifExit);
        } else {
            LLVMBasicBlockRef ifBlock = LLVMAppendBasicBlock(currentFunction, newBlock("IfTrue"));
            LLVMBasicBlockRef ifExit = LLVMAppendBasicBlock(currentFunction, newBlock("IfExit"));
            
            LLVMValueRef cond = visit(ctx.cond());
            cond = LLVMBuildICmp(builder, LLVMIntNE, cond, LLVMConstInt(i32Type, 0, 0), newConditionVar());
            // if (true) ifBlock else ifExit;
            if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
                Blockbr.put(LLVMGetInsertBlock(builder), 1);
                LLVMBuildCondBr(builder, cond, ifBlock, ifExit);
            }
            
            LLVMPositionBuilderAtEnd(builder, ifBlock);
            visit(ctx.stat(0));
            if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) == null) {
                Blockbr.put(LLVMGetInsertBlock(builder), 1);
                LLVMBuildBr(builder, ifExit);
            }
            // LLVMBasicBlockRef finishIfBlock = LLVMGetInsertBlock(builder);
            // LLVMPositionBuilderAtEnd(builder, ifBlock);
            // br ifExit;
            
            // LLVMPositionBuilderAtEnd(builder, currentBlock);
            // if (LLVMGetBasicBlockTerminator(finishIfBlock) == null) {
                // LLVMPositionBuilderAtEnd(builder, ifBlock);
            // }

            LLVMPositionBuilderAtEnd(builder, ifExit);
        }

        return null;
    }

	@Override public LLVMValueRef visitReturn(SysYParser.ReturnContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }
        LLVMValueRef tmp = visit(ctx.exp());

        LLVMBuildRet(builder, tmp);

        return new LLVMValueRef();
    }
    
	@Override public LLVMValueRef visitExpd(SysYParser.ExpdContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }
        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }

    @Override public LLVMValueRef visitVarAssign(SysYParser.VarAssignContext ctx) {
        if (Blockbr.getOrDefault(LLVMGetInsertBlock(builder), null) != null) {
            return null;
        }
        LLVMValueRef lval = visit(ctx.lVal());
        visit(ctx.ASSIGN());
        LLVMValueRef exp = visit(ctx.exp());
        // String name = ctx.lVal().getText();

        LLVMBuildStore(builder, exp, lval);

        return lval;
    }

    @Override public LLVMValueRef visitUnaryOp1(SysYParser.UnaryOp1Context ctx) {
        LLVMValueRef tmp = visitChildren(ctx);
        String str = ctx.getText();
        LLVMValueRef result;
        if (str.charAt(0) == '+') {
            result = tmp;
        } else if (str.charAt(0) == '-') {
            result = LLVMBuildSub(builder, zero, tmp, "result");
        } else {
            if (tmp.equals(zero)) {
                result = LLVMConstInt(i32Type, 1, 0);
            } else {
                result = zero;
            }
        }

        return result;
    }
    @Override public LLVMValueRef visitMulOp(SysYParser.MulOpContext ctx) {
        LLVMValueRef left = visit(ctx.exp(0));
        int opType = 0;
        if (ctx.MUL() != null) {
            visit(ctx.MUL());
            opType = 1;
        } else if (ctx.DIV() != null) {
            visit(ctx.DIV());
            opType = 2;
        } else {
            visit(ctx.MOD());
            opType = 3;
        }
        LLVMValueRef right = visit(ctx.exp(1));

        LLVMValueRef result;
        if (opType == 1) {
            result = LLVMBuildMul(builder, left, right, "result");
        } else if (opType == 2) {
            result = LLVMBuildSDiv(builder, left, right, "result");
        } else {
            result = LLVMBuildSRem(builder, left, right, "result");
        }

        return result;
    }
    @Override public LLVMValueRef visitPlusOp(SysYParser.PlusOpContext ctx) {
        LLVMValueRef left = visit(ctx.exp(0));
        int opType = 0;
        if (ctx.PLUS() != null) {
            visit(ctx.PLUS());
            opType = 1;
        } else {
            visit(ctx.MINUS());
            opType = 2;
        }
        LLVMValueRef right = visit(ctx.exp(1));

        LLVMValueRef result;
        if (opType == 1) {
            result = LLVMBuildAdd(builder, left, right, "result");
        } else {
            result = LLVMBuildSub(builder, left, right, "result");
        }

        return result;
    }
    @Override public LLVMValueRef visitAndOp(SysYParser.AndOpContext ctx) {
        LLVMValueRef left = visit(ctx.exp(0));
        int opType = 0;
        if (ctx.BIT_AND() != null) {
            visit(ctx.BIT_AND());
            opType = 1;
        } else {
            visit(ctx.BIT_OR());
            opType = 2;
        }
        LLVMValueRef right = visit(ctx.exp(1));

        LLVMValueRef result;
        if (opType == 1) {
            result = LLVMBuildAnd(builder, left, right, "result");
        } else {
            result = LLVMBuildOr(builder, left, right, "result");
        }

        return result;
    }

    @Override public LLVMValueRef visitAllType(SysYParser.AllTypeContext ctx) { 
        return visitChildren(ctx); 
    }

    @Override public LLVMValueRef visitIntDeclare(SysYParser.IntDeclareContext ctx) {
        if (ctx.CONST() != null) {
            visit(ctx.CONST());
        }
        visit(ctx.INT());
        visit(ctx.IDENT());
        String name = ctx.IDENT().getText();

        LLVMValueRef var;
        if (currentScope.getScopeName().equals("GlobalScope#1")) {
            var = LLVMAddGlobal(module, i32Type, name);
            if (ctx.ASSIGN() != null) {
                visit(ctx.ASSIGN());
                LLVMValueRef value = visit(ctx.exp());
                LLVMSetInitializer(var, value);
            } else {
                LLVMSetInitializer(var, zero);
            }
        } else {
            LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
            
            LLVMValueRef terminator = LLVMGetBasicBlockTerminator(currentFunctionEntry);
            if (terminator != null) {
                LLVMPositionBuilderBefore(builder, terminator);
            } else {
                LLVMPositionBuilderAtEnd(builder, currentFunctionEntry);
            }
            var = LLVMBuildAlloca(builder, i32Type, name);

            if (ctx.ASSIGN() != null) {
                LLVMPositionBuilderAtEnd(builder, currentBlock);
                visit(ctx.ASSIGN());
                LLVMValueRef value = visit(ctx.exp());
                LLVMBuildStore(builder, value, var);
            }
        }
        currentScope.define(name, var);

        return new LLVMValueRef();
    }

    @Override public LLVMValueRef visitArrayDeclare(SysYParser.ArrayDeclareContext ctx) {
        return visitChildren(ctx);
    }

    @Override public LLVMValueRef visitFuncDecl(SysYParser.FuncDeclContext ctx) {
        return visitChildren(ctx); 
    }

    @Override public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        visitChildren(ctx);
        String varName = ctx.IDENT().getText();
        LLVMValueRef pointer = currentScope.resolve(varName);

        return pointer;
    }

    // get Expr type.
    @Override public LLVMValueRef visitPAREN(SysYParser.PARENContext ctx) {
        visit(ctx.L_PAREN());
        LLVMValueRef tmp = visit(ctx.exp());
        visit(ctx.R_PAREN());

        return tmp;
    }
    @Override public LLVMValueRef visitVarCall(SysYParser.VarCallContext ctx) {
        LLVMValueRef pointer = visitChildren(ctx);
        String name = ctx.lVal().IDENT().getText();

        LLVMTypeRef type = LLVMTypeOf(pointer);
        LLVMValueRef value;
        if (LLVMGetTypeKind(type) == LLVMPointerTypeKind) {
            value = LLVMBuildLoad(builder, pointer, name);
        } else {
            value = pointer;
            
        }
        
        return value;
    }
    @Override public LLVMValueRef visitConstInt(SysYParser.ConstIntContext ctx) {
        return visitChildren(ctx);
    }

    public String newBlock(String name) {
        ++blockCnt;
        return name + blockCnt;
    }
    public String newConditionVar() {
        ++conditionVarCnt;
        return "cond" + conditionVarCnt;
    }
}
