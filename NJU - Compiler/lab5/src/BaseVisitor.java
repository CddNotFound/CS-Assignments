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

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class BaseVisitor extends SysYParserBaseVisitor<LLVMValueRef>{
    Scope currentScope;

    // lab 4:
    int varCnt, blockCnt, conditionVarCnt;

    LLVMModuleRef module;
    LLVMBuilderRef builder;
    LLVMTypeRef i32Type, i1Type;
    LLVMValueRef zero, one;

    LLVMValueRef currentFunction;

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
        // System.out.println("visit block.");
        LLVMValueRef tmp = visitChildren(ctx);
        
        return tmp;
    }

    @Override public LLVMValueRef visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
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
        
        // System.out.println("THere will be a declare.");


        
        String str = ctx.funcDecl().IDENT().getText() + "Entry";
        LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, str);
        LLVMPositionBuilderAtEnd(builder, block1);

        for (int i = 0 ; i < paraCnt; i++) {
            LLVMValueRef x = LLVMGetParam(function, i);
            // System.out.println(x == null);
            String varName = ((SysYParser.ParameterIntContext)ctx.funcDecl().parameters().parameter(i)).IDENT().getText();
            
            if (currentScope.countCurrentScope(varName) != null) {
                continue;
            }

            LLVMValueRef var = LLVMBuildAlloca(builder, i32Type, varName);
            LLVMBuildStore(builder, x, var);
            
            // System.out.println("define var name = " + varName);
            currentScope.define(varName, var);
        }

        LLVMValueRef tmp = visitChildren(ctx);

        currentScope = currentScope.getEnclosScope();
        currentFunction = null;
        
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
        // System.out.println("num Decimal = " + res);
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
        // System.out.println("There will be a function call.");
        visit(ctx.IDENT());

        String funcName = ctx.IDENT().getText();
        LLVMValueRef function = currentScope.resolve(funcName);

        int paraCnt = 0;
        if (ctx.funcRParams() != null) {
            paraCnt = ctx.funcRParams().param().size();
        }

        PointerPointer<LLVMValueRef> paras = new PointerPointer<>(paraCnt);
        for (int i = 0; i < paraCnt; i++) {
            // if (i > 0) {
            //     visit(ctx.funcRParams().COMMA(i));
            // }
            LLVMValueRef x = visit(ctx.funcRParams().param(i).exp());
            // System.out.println("para " + i + "is " + x.toString());
            paras.put(i, x);
        }

        LLVMTypeRef funcType = LLVMGetElementType(LLVMTypeOf(function));
        return LLVMBuildCall2(builder, funcType, function, paras, paraCnt, funcName);
    }

	@Override public LLVMValueRef visitVariableDecl(SysYParser.VariableDeclContext ctx) {    
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
        LLVMValueRef left = visit(ctx.cond(0));
        LLVMValueRef right = visit(ctx.cond(1));

        LLVMValueRef result = LLVMBuildAnd(builder, left, right, "result");
        result = LLVMBuildZExt(builder, result, i32Type, "result");
        return result;
    }
    @Override public LLVMValueRef visitLogicalOr(SysYParser.LogicalOrContext ctx) {
        LLVMValueRef left = visit(ctx.cond(0));
        LLVMValueRef right = visit(ctx.cond(1));

        LLVMValueRef result = LLVMBuildOr(builder, left, right, "result");
        result = LLVMBuildZExt(builder, result, i32Type, "result");
        return result;
    }

    @Override public LLVMValueRef visitWhileLoop(SysYParser.WhileLoopContext ctx) {
        LLVMBasicBlockRef conditionCheck = LLVMAppendBasicBlock(currentFunction, newBlock("WhileCondition"));
        LLVMBasicBlockRef block = LLVMAppendBasicBlock(currentFunction, newBlock("WhileLoop"));
        LLVMBasicBlockRef exit = LLVMAppendBasicBlock(currentFunction, newBlock("WhileExit"));
        
        LLVMBuildBr(builder, conditionCheck);  
        LLVMPositionBuilderAtEnd(builder, conditionCheck);
        LLVMValueRef cond = visit(ctx.cond());
        cond = LLVMBuildICmp(builder, LLVMIntNE, cond, LLVMConstInt(i32Type, 0, 0), newConditionVar());
        LLVMBuildCondBr(builder, cond, block, exit);

        LLVMPositionBuilderAtEnd(builder, block);
        visit(ctx.stat());
        LLVMBuildBr(builder, conditionCheck);

        LLVMPositionBuilderAtEnd(builder, exit);

        
        return null;
    }

    @Override public LLVMValueRef visitIfElse(SysYParser.IfElseContext ctx) {
        LLVMBasicBlockRef currentBlock = LLVMGetInsertBlock(builder);
        if (ctx.ELSE() != null) {
            LLVMBasicBlockRef ifBlock = LLVMAppendBasicBlock(currentFunction, newBlock("IfTrue"));
            LLVMBasicBlockRef elseBlock = LLVMAppendBasicBlock(currentFunction, newBlock("IfFalse"));
            LLVMBasicBlockRef ifExit = LLVMAppendBasicBlock(currentFunction, newBlock("IfExit"));
            
            LLVMValueRef cond = visit(ctx.cond());
            cond = LLVMBuildICmp(builder, LLVMIntNE, cond, LLVMConstInt(i32Type, 0, 0), newConditionVar());
            // if (true) ifBlock else elseBlock;
            LLVMBuildCondBr(builder, cond, ifBlock, elseBlock);
            
            LLVMPositionBuilderAtEnd(builder, ifBlock);
            visit(ctx.stat(0));
            LLVMBuildBr(builder, ifExit);
            // LLVMBasicBlockRef finishIfBlock = LLVMGetInsertBlock(builder);
            
            // LLVMPositionBuilderAtEnd(builder, ifBlock);
            // br ifExit
            
            LLVMPositionBuilderAtEnd(builder, elseBlock);
            visit(ctx.stat(1));
            LLVMBuildBr(builder, ifExit);
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
            LLVMBuildCondBr(builder, cond, ifBlock, ifExit);
            
            LLVMPositionBuilderAtEnd(builder, ifBlock);
            visit(ctx.stat(0));
            LLVMBuildBr(builder, ifExit);
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
        // visit(ctx.RETURN());
        // System.out.println("visit, return " + ctx.exp().getText());
        LLVMValueRef tmp = visit(ctx.exp());
        // System.out.println(tmp.toString());
        // visit(ctx.SEMICOLON());

        LLVMBuildRet(builder, tmp);

        return new LLVMValueRef();
    }
    
	@Override public LLVMValueRef visitExpd(SysYParser.ExpdContext ctx) {
        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }

    @Override public LLVMValueRef visitVarAssign(SysYParser.VarAssignContext ctx) {
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
            // System.out.println("Op = +");
            result = tmp;
        } else if (str.charAt(0) == '-') {
            // System.out.println("Op = -");
            result = LLVMBuildSub(builder, zero, tmp, "result");
        } else {
            // System.out.println("Op = !");
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

        // System.out.println("left = " + left.toString() + " + " + "right = " + right.toString());
        // System.out.println(ctx.exp(0).getText() + " + " + ctx.exp(1).getText());
        // // System.out.println("right = " + (right == null));

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
        // System.out.println(currentScope.getScopeName() + " define: " + name);
        if (currentScope.getScopeName().equals("GlobalScope#1")) {
            // System.out.println("A new GlobalVar.");
            var = LLVMAddGlobal(module, i32Type, name);
            if (ctx.ASSIGN() != null) {
                visit(ctx.ASSIGN());
                LLVMValueRef value = visit(ctx.exp());
                LLVMSetInitializer(var, value);
            } else {
                LLVMSetInitializer(var, zero);
            }
        } else {
            var = LLVMBuildAlloca(builder, i32Type, name);
            if (ctx.ASSIGN() != null) {
                visit(ctx.ASSIGN());
                LLVMValueRef value = visit(ctx.exp());
                LLVMBuildStore(builder, value, var);
            }
        }
        currentScope.define(name, var);

        return new LLVMValueRef();
    }

    @Override public LLVMValueRef visitArrayDeclare(SysYParser.ArrayDeclareContext ctx) {
        // System.out.println("Error: Can't declare an array!");

        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }

    @Override public LLVMValueRef visitFuncDecl(SysYParser.FuncDeclContext ctx) {
        LLVMValueRef tmp = visitChildren(ctx); 

        return tmp;
    }


    @Override public LLVMValueRef visitLVal(SysYParser.LValContext ctx) {
        visitChildren(ctx);
        String varName = ctx.IDENT().getText();
        // System.out.println("visit lval = " + varName);
        LLVMValueRef pointer = currentScope.resolve(varName);
        // System.out.println(pointer.toString());
        // LLVMValueRef value = LLVMBuildLoad(builder, pointer, varName);

        // System.out.println("get lval name : " + varName);

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
        // visitChildren(ctx);
        // LLVMValueRef tmp = new LLVMValueRef();

        // return tmp;
    }

    public String getType(Type type) {
        if (type == null) {
            return "null";
        } else if (type instanceof IntType) {
            return "Int";
        } else if (type instanceof ArrayType) {
            String result = "Int";
            Type currentType = type;
            while (currentType instanceof ArrayType) {
                result += "[]";
                currentType = ((ArrayType)currentType).elementType;
            } 
            return result;
        } else {
            return "Function";
        }
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
