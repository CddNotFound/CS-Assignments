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

    // lab 4:
    int varCnt, blockCnt;

    LLVMModuleRef module;
    LLVMBuilderRef builder;
    LLVMTypeRef i32Type;
    LLVMValueRef zero, one;

    public BaseVisitor() {
        varCnt = blockCnt = 0;

        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget(); 

        module = LLVMModuleCreateWithName("module");
        builder = LLVMCreateBuilder();
        i32Type = LLVMInt32Type();
        zero = LLVMConstInt(i32Type, 0, 0);
        one = LLVMConstInt(i32Type, 1, 0);
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
        LLVMValueRef tmp = visitChildren(ctx);
        
        return tmp;
    }

    @Override public LLVMValueRef visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
        LLVMTypeRef returnType = i32Type;
        PointerPointer<Pointer> argumentTypes = new PointerPointer<>(0);
        LLVMTypeRef ft = LLVMFunctionType(returnType, argumentTypes, 0, 0);
        LLVMValueRef function = LLVMAddFunction(module, ctx.funcDecl().IDENT().getText(), ft);
        
        // New Scope
        currentScope = new Scope("functionScope", currentScope);

        String str = ctx.funcDecl().IDENT().getText() + "Entry";
        LLVMBasicBlockRef block1 = LLVMAppendBasicBlock(function, str);
        LLVMPositionBuilderAtEnd(builder, block1);
        // System.out.println("set block to " + str);

        LLVMValueRef tmp = visitChildren(ctx);

        currentScope = currentScope.getEnclosScope();
        
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
        LLVMValueRef tmp = new LLVMValueRef();
        
        visitChildren(ctx);
        
        return tmp;
    }

	@Override public LLVMValueRef visitVariableDecl(SysYParser.VariableDeclContext ctx) {    
        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }

    @Override public LLVMValueRef visitWhileLoop(SysYParser.WhileLoopContext ctx) {
        LLVMValueRef tmp = visitChildren(ctx);

        return tmp;
    }

    @Override public LLVMValueRef visitIfElse(SysYParser.IfElseContext ctx) {
        LLVMValueRef tmp = visitChildren(ctx);
        
        return tmp;
    }

	@Override public LLVMValueRef visitReturn(SysYParser.ReturnContext ctx) {
        visit(ctx.RETURN());
        LLVMValueRef tmp = visit(ctx.exp());
        visit(ctx.SEMICOLON());

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
        LLVMValueRef pointer = currentScope.resolve(varName);
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
        LLVMValueRef value = LLVMBuildLoad(builder, pointer, name);
        
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
}
