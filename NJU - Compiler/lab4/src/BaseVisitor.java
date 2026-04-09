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

public class BaseVisitor extends SysYParserBaseVisitor<T>{
    Scope currentScope;
    ParseTreeProperty<Type> nodeTypes;

    // lab 4:
    int varCnt;

    public BaseVisitor() {
        nodeTypes = new ParseTreeProperty<Type>();
        varCnt = 0;
    }

    @Override public T visitProg(SysYParser.ProgContext ctx) {
        currentScope = new Scope("GlobalScope", null);
        T tmp = visitChildren(ctx); 
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitTerminal(TerminalNode node) {
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

        T tmp = new T();
        return tmp;
    }

    @Override public T visitBlock(SysYParser.BlockContext ctx) {
        T tmp = visitChildren(ctx);
        
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
        T tmp = visitChildren(ctx);
        
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitParameters(SysYParser.ParametersContext ctx) {
        T tmp = visitChildren(ctx);

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }
    
    @Override public T visitFunctionCall(SysYParser.FunctionCallContext ctx) {
        T tmp = new T();
        
        visitChildren(ctx);
        
        tmp.type = new IntType();
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

	@Override public T visitVariableDecl(SysYParser.VariableDeclContext ctx) {    
        T tmp = visitChildren(ctx);

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitWhileLoop(SysYParser.WhileLoopContext ctx) {
        T tmp = visitChildren(ctx);

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitIfElse(SysYParser.IfElseContext ctx) {
        T tmp = visitChildren(ctx);
        
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

	@Override public T visitReturn(SysYParser.ReturnContext ctx) {
        visitChildren(ctx);

        return new T();
    }
    
	@Override public T visitExpd(SysYParser.ExpdContext ctx) {
        T tmp = visitChildren(ctx);

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitVarAssign(SysYParser.VarAssignContext ctx) {
        visitChildren(ctx);

        Type leftInfo = nodeTypes.get(ctx.lVal());
        Type rightInfo = nodeTypes.get(ctx.exp());
        
        T tmp = new T();
        tmp.type = leftInfo;

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitUnaryOp1(SysYParser.UnaryOp1Context ctx) {
        visitChildren(ctx);

        T tmp = new T();
        tmp.type = new IntType();
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }
    @Override public T visitMulOp(SysYParser.MulOpContext ctx) {
        visitChildren(ctx);

        T tmp = new T();
        tmp.type = new IntType();

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }
    @Override public T visitPlusOp(SysYParser.PlusOpContext ctx) {
        visitChildren(ctx);

        T tmp = new T();
        tmp.type = new IntType();

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }
    @Override public T visitAndOp(SysYParser.AndOpContext ctx) {
        visitChildren(ctx);
        T tmp = new T();

        tmp.type = new IntType();

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitAllType(SysYParser.AllTypeContext ctx) { 
        return visitChildren(ctx); 
    }

    @Override public T visitIntDeclare(SysYParser.IntDeclareContext ctx) {
        T tmp = visitChildren(ctx);
        
        int intCnt = ctx.IDENT().size();
        for (int i = 0; i < intCnt; i++) {
            currentScope.define(ctx.IDENT(i).getText(), new IntType());
        }

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitArrayDeclare(SysYParser.ArrayDeclareContext ctx) {
        System.out.println("Error: Can't declare an array!");

        T tmp = visitChildren(ctx);

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    @Override public T visitFuncDecl(SysYParser.FuncDeclContext ctx) {
        T tmp = visitChildren(ctx); 

        FunctionType function = new FunctionType();
        function.retType = new IntType();

        currentScope.define(ctx.IDENT().getText(), function);

        // New Scope
        currentScope = new Scope("functionScope", currentScope);

        if (ctx.parameters() != null) {
            int paraCnt = ctx.parameters().parameter().size();
            for (int i = 0; i < paraCnt; i++) {
                SysYParser.ParametersContext paras = ctx.parameters();
                
                Type paraType;
                String varName;
                if (paras.parameter(i) instanceof SysYParser.ParameterArrayContext) {
                    paraType = new ArrayType();
                    varName = ((SysYParser.ParameterArrayContext)paras.parameter(i)).IDENT().getText();
                } else {
                    paraType = new IntType();
                    varName = ((SysYParser.ParameterIntContext)paras.parameter(i)).IDENT().getText();
                }
                
                if (currentScope.countCurrentScope(varName) != null) {
                    continue;
                }

                currentScope.define(varName, paraType);
            }
        }

        // define function in globalScope
        currentScope = currentScope.getEnclosScope();
        currentScope.define(ctx.IDENT().getText(), function);

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }


    // Error type 1: Undefined variable.
    @Override public T visitLVal(SysYParser.LValContext ctx) {
        visitChildren(ctx);
        String varName = ctx.IDENT().getText();
        Type varType = currentScope.resolve(varName);

        int bracktCnt = ctx.L_BRACKT().size();
        Type currentType = varType;
        for (int i = 0; i < bracktCnt; i++) {
            currentType = ((ArrayType)currentType).elementType;
        }

        T tmp = new T();
        tmp.type = currentType;

        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }

    // get Expr type.
    @Override public T visitPAREN(SysYParser.PARENContext ctx) {
        visitChildren(ctx);
        T tmp = new T();
        tmp.type = nodeTypes.get(ctx.exp());
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }
    @Override public T visitVarCall(SysYParser.VarCallContext ctx) {
        visitChildren(ctx);
        T tmp = new T();
        tmp.type = nodeTypes.get(ctx.lVal());
        nodeTypes.put(ctx, tmp.type);
        return tmp;
    }
    @Override public T visitConstInt(SysYParser.ConstIntContext ctx) {
        visitChildren(ctx);
        T tmp = new T();
        tmp.type = new IntType();

        nodeTypes.put(ctx, tmp.type);
        return tmp;
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
}
