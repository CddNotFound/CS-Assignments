import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

// import SysYParser.CodeBlockContext;
// import SysYParser.IfElseContext;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.*;
import java.util.HashMap;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class BaseVisitor extends SysYParserBaseVisitor<T>{
    // HashMap<Integer, TokenStyle> mp;
    // int parenCnt, bracktCnt, braceCnt;
    int braceCnt;
    int level;
    int color;
    boolean underline, blockInBlock, declare, unaryOp;
    boolean firstRow;
    public static final int[] colorList = new int[6];
    int a, b;

    Scope currentScope;
    // ArrayList<Type> functionList;
    ArrayList<String> errorList;

    static {
        for (int i = 0; i < 6; i++) {
            colorList[i] = 91 + i;
        }
    }

    public BaseVisitor() {
        // mp = new HashMap<Integer, TokenStyle>();
        this.errorList = new ArrayList<String>();
        // functionList = new ArrayList<Type>();
        this.level = 0;
        this.braceCnt = 0;
        this.color = 0;
        this.underline = false;
        this.unaryOp = false;
        this.blockInBlock = false;
        this.declare = false;
        this.firstRow = true;
        a = 0;
        b = 0;
    }

    private void printTab() {
        for (int i = 1; i <= level; i++) {
            System.out.print("    ");
        }
    }
    private void println() {
        System.out.println();
    }

    private void printToken(String text, int color, boolean underline) {
        firstRow = false;
        if (text == "<EOF>") {
            return ;
        }

        if (color == 0) {
            System.out.print(text);
        } else if (underline && !text.trim().isEmpty()){
                System.out.print("\033[" + color + ";4m" + text + "\033[0m");
        } else {
            System.out.print("\033[" + color + "m" + text + "\033[0m");
        }
    }

    private void printPreviousSpace(int type) {
        if (((type >= 12 && type <= 21) || (type >= 23 && type <= 26))
         || ((type == 10 || type == 11) && !unaryOp)) {
            System.out.print(" ");
        }
    }

    private void printFollowingSpace(int type) {
        if (((type >= 1 && type <= 3) || type == 7 || type == 8 || type == 4 || type == 6)
         || (type >= 12 && type <= 21 || (type >= 23 && type <= 26))
         || ((type == 10 || type == 11) && !unaryOp)
         || type == 27) {
            System.out.print(" ");
        }
    }


    @Override public T visitProg(SysYParser.ProgContext ctx) {
        Interval interval = ctx.getSourceInterval();
        a = interval.a;
        b = interval.b;
        return visitChildren(ctx); 
    }

    @Override public T visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        int type = token.getType();
        String text = token.getText();
        int idx = token.getTokenIndex();

        int sColor = color;
        boolean sUnderline = underline;

        if (type >= 1 && type <= 9) { // key word : Bright Cyan
            color = 96;
        } else if (type >= 10 && type <= 28) { // operator : Bright Red
            color = 91;
        } else if (type == 29) { // const int : Magenta
            color = 35;
        } else if (type == 37 || type == 35 || type == 39) {
            color = colorList[braceCnt % 6];
            ++braceCnt;
        } else if (type == 38 || type == 36 || type == 40) {
            --braceCnt;
            color = colorList[braceCnt % 6];
        }

        printPreviousSpace(type);

        if (declare) {
            // System.err.print("delcare: ");
            printToken(text, color, underline);
        } else if (type == 37) {
            // System.err.print("L_BRACE: ");
            if (blockInBlock) {
                println();
                printTab();
                printToken(text, color, underline);
                level += 1;
            } else {
                ParseTree parent = node.getParent().getParent().getParent();
                if (parent instanceof SysYParser.IfElseContext) {
                    SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) parent;
                    if (!(ftr.stat().size() >= 2 && ftr.stat(1) == node.getParent().getParent())) {
                        System.out.print(" ");
                    }
                } else {
                    System.out.print(" ");
                }
                printToken(text, color, underline);
                level += 1;
            }
        } else if (type == 38) {
            // System.err.print("R_BRACE: ");
            println();
            level -= 1;
            printTab();
            printToken(text, color, underline);
        } else if (type == 8) {
            SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) node.getParent();
            if (!(ftr.stat(0) instanceof SysYParser.CodeBlockContext)) {
                // level -= 1;
            }
            println();
            printTab();
            printToken(text, color, underline);
        } else {
            printToken(text, color, underline);
        }

        printFollowingSpace(type);

        color = sColor;
        underline = sUnderline;

        return (T)this.defaultResult();
    }

    @Override public T visitBlock(SysYParser.BlockContext ctx) {
        
        ParseTree parent = ctx.getParent().getParent();
        if (parent instanceof SysYParser.IfElseContext
         || parent instanceof SysYParser.WhileLoopContext
         || parent instanceof SysYParser.FunctionDeclContext) {
            blockInBlock = false;
        } else {
            blockInBlock = true;
        }

        T tmp = visitChildren(ctx);
    
        return tmp;
    }

    @Override public T visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
        if (!firstRow) {
            println();
            println();
        }
        printTab();
        int sColor = color;
        boolean sUnderline = underline;

        color = 93;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;
        
        return tmp;
    }

    @Override public T visitParameters(SysYParser.ParametersContext ctx) {
        int sColor = color;
        boolean sUnderline = underline;

        color = 0;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;
        
        return tmp;
    }
    
    @Override public T visitFunctionCall(SysYParser.FunctionCallContext ctx) {
        int sColor = color;
        boolean sUnderline = underline;
        String funcName = ctx.IDENT().getText();
        Type resolveType = currentScope.resolve(funcName);

        // Error type 2: Undefined function.
        if (resolveType == null) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 2 at Line " + startToken.getLine() + ": Undefined function:" + ctx.IDENT().getText() + ".");
        }

        // Error type 10: Called object type Int is not a function.
        if (resolveType instanceof IntType) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 10 at Line " + startToken.getLine() + ": Called object type Int is not a function.");
        }

        // Error type 8: Incompatible function arguments.
        FunctionType funcType = (FunctionType)resolveType;
        int paraCnt = funcType.parametersType.size();
        int callCnt = ctx.funcRParams().param().size();
        if (paraCnt != callCnt) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 8 at Line " + startToken.getLine() + ": Incompatible function arguments.");
        }
        for (int i = 0; i < paraCnt; i++) {
            Type paraType = funcType.parametersType.get(i);
            Type callType = visit(ctx.funcRParams().param(i)).type;
            if (paraType.getClass() != callType.getClass()) {
                Token startToken = ctx.getStart();
                errorList.add("Error type 8 at Line " + startToken.getLine() + ": Incompatible function arguments.");
            }
        }

        color = 93;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;

        // return type
        FunctionType function = (FunctionType) funcType;
        tmp.type = function.retType;

        return tmp;
    }

	@Override public T visitVariableDecl(SysYParser.VariableDeclContext ctx) {    
        stmtTab(ctx.getParent());
        int sColor = color;
        boolean sUnderline = underline, sDeclare = declare;

        color = 95;
        underline = true;
        declare = true;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;
        declare = sDeclare;

        exitStmt(ctx.getParent());
        return tmp;
    }

    @Override public T visitWhileLoop(SysYParser.WhileLoopContext ctx) {
        stmtTab(ctx.getParent());
        int sColor = color;
        boolean sUnderline = underline;

        color = 97;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;

        exitStmt(ctx.getParent());
        return tmp;
    }

    @Override public T visitIfElse(SysYParser.IfElseContext ctx) {
        ParseTree parent = ctx.getParent();
        if (parent instanceof SysYParser.IfElseContext) {
            SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) parent;
            if (ftr.stat().size() > 1 && ftr.stat(1) == ctx) {
                // System.out.print(" ");
            } else {
                println();
                level += 1;
                printTab();
                // level -= 1;
            }
        } else {
            println();
            printTab();
        }
        int sColor = color;
        boolean sUnderline = underline;

        color = 97;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;

        if (parent instanceof SysYParser.IfElseContext) {
            SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) parent;
            if (ftr.stat().size() > 1 && ftr.stat(1) == ctx) {
                // System.out.print(" ");
            } else {
                level -= 1;
            }
        }
        // println();
        
        return tmp;
    }

	@Override public T visitReturn(SysYParser.ReturnContext ctx) {
        stmtTab(ctx.getParent());
        int sColor = color;
        boolean sUnderline = underline;

        color = 97;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;
        
        exitStmt(ctx.getParent());

        // Error type 7: Unmatched return type.
        Type retType = tmp.type;
        if (!(retType instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 7 at Line " + startToken.getLine() + ": Unmatched return type: expected Int, but found " + getType(retType) + ".");
        }

        return tmp;
    }
    
	@Override public T visitExpd(SysYParser.ExpdContext ctx) {
        stmtTab(ctx.getParent());
        int sColor = color;
        boolean sUnderline = underline;

        color = 97;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;
        
        exitStmt(ctx.getParent());
        return tmp;
    }

    @Override public T visitVarAssign(SysYParser.VarAssignContext ctx) {
        stmtTab(ctx.getParent());
        int sColor = color;
        boolean sUnderline = underline;
        
        color = 97;
        underline = false;
        
        T tmp = visitChildren(ctx);
        
        color = sColor;
        underline = sUnderline;
        
        exitStmt(ctx.getParent());
        T leftInfo = visit(ctx.lVal());
        T rightInfo = visit(ctx.exp());
        
        // Error Type 11: Cannot assign a value to a function 'f'.
        if (leftInfo.type instanceof FunctionType) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 11 at Line " + startToken.getLine() + ": Cannot assign a value to a function " + ctx.lVal().getText() + ".");
        }


        // Error Type 5: Unmatched assign type.
        if (leftInfo.type.getClass() != rightInfo.type.getClass()) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 5 at Line " + startToken.getLine() + ": Unmatched assign type:" + getType(leftInfo.type) + " and " + getType(rightInfo.type) + ".");
        }

        return tmp;
    }

    @Override public T visitUnaryOp1(SysYParser.UnaryOp1Context ctx) {
        boolean sUnaryOp = unaryOp;

        unaryOp = true;

        T tmp = visitChildren(ctx);

        unaryOp = sUnaryOp;

        // Error Type 6: Unmatched unaryOp type.
        T expInfo = visit(ctx.exp());
        if (!(expInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched unaryOp type: expected Int, but found " + getType(expInfo.type) + ".");
        }

        return tmp;
    }
    @Override public T visitMulOp(SysYParser.MulOpContext ctx) {
        T tmp = visitChildren(ctx);

        // Error Type 6: Unmatched mul/div/mod type.
        T leftInfo = visit(ctx.exp(1));
        T rightInfo = visit(ctx.exp(2));
        if (!(leftInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched mul/div/mod type: expected Int, but found " + getType(leftInfo.type) + ".");
        }
        if (!(rightInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched mul/div/mod type: expected Int, but found " + getType(rightInfo.type) + ".");
        }

        return tmp;
    }
    @Override public T visitPlusOp(SysYParser.PlusOpContext ctx) {
        T tmp = visitChildren(ctx);

        // Error Type 6: Unmatched plus/minus type.
        T leftInfo = visit(ctx.exp(1));
        T rightInfo = visit(ctx.exp(2));
        if (!(leftInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched plus/minus type: expected Int, but found " + getType(leftInfo.type) + ".");
        }
        if (!(rightInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched plus/minus type: expected Int, but found " + getType(rightInfo.type) + ".");
        }

        return tmp;
    }
    @Override public T visitAndOp(SysYParser.AndOpContext ctx) {
        T tmp = visitChildren(ctx);

        // Error Type 6: Unmatched and/or type.
        T leftInfo = visit(ctx.exp(1));
        T rightInfo = visit(ctx.exp(2));
        if (!(leftInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched and/or type: expected Int, but found " + getType(leftInfo.type) + ".");
        }
        if (!(rightInfo.type instanceof IntType)) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 6 at Line " + startToken.getLine() + ": Unmatched and/or type: expected Int, but found " + getType(rightInfo.type) + ".");
        }

        return tmp;
    }

    public void stmtTab(ParseTree parent) {
        if (parent instanceof SysYParser.IfElseContext
         || parent instanceof SysYParser.WhileLoopContext) {
            println();
            level += 1;
            printTab();
        } else {
            if (!firstRow) {
                println();
            }
            printTab();
        }
    }

    public void exitStmt(ParseTree parent) {
        if (parent instanceof SysYParser.IfElseContext
         || parent instanceof SysYParser.WhileLoopContext) {
            level -= 1;
        }
    }

    @Override public T visitAllType(SysYParser.AllTypeContext ctx) { 
        return visitChildren(ctx); 
    }

    @Override public T visitIntDeclare(SysYParser.IntDeclareContext ctx) {
        
        T tmp = visitChildren(ctx);
        
        int intCnt = ctx.IDENT().size();
        for (int i = 0; i < intCnt; i++) {
            // Error type 3: Redefined variable.
            if (currentScope.countCurrentScope(ctx.IDENT(i).getText()) != null) {
                Token startToken = ctx.getStart();
                errorList.add("Error type 3 at Line " + startToken.getLine() + ": Redefined variable:" + ctx.IDENT(i).getText() + ".");
            }

            currentScope.define(ctx.IDENT(i).getText(), new IntType());
        }

        return tmp;
    }

    @Override public T visitArrayDeclare(SysYParser.ArrayDeclareContext ctx) {
        // Error type 3: Redefined variable.
        if (currentScope.countCurrentScope(ctx.IDENT().getText()) != null) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 3 at Line " + startToken.getLine() + ": Redefined variable:" + ctx.IDENT().getText() + ".");
        }

        T tmp = visitChildren(ctx);

        // int arraySize = 1;
        int numCnt = ctx.number().size();
        Type currentType = new IntType();
        for (int i = numCnt - 1; i >= 0; i--) {
            String number = ctx.number(i).getText();
            int oneSize = 1;
            for (int j = 0; j < number.length(); j++) {
                oneSize = oneSize * 10 + number.charAt(i) - 48;
            }
            currentType = new ArrayType(currentType, oneSize);
        }

        currentScope.define(ctx.IDENT().getText(), currentType);

        return tmp;
    }

    @Override public T visitFuncDecl(SysYParser.FuncDeclContext ctx) {
        // Error type 4: Redefined function.
        String funcName = ctx.IDENT().getText();
        if (currentScope.resolve(funcName) != null) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 4 at Line " + startToken.getLine() + ": Redefined function:" + funcName + ")");
            return null;
        }


        // New Scope
        // functionList.add(new IntType());

        T tmp = visitChildren(ctx); 

        FunctionType function = new FunctionType();
        function.retType = new IntType();
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

        currentScope.define(ctx.IDENT().getText(), function);

        return tmp;
    }


    // Error type 1: Undefined variable.
    @Override public T visitLVal(SysYParser.LValContext ctx) {
        T tmp = visitChildren(ctx);
        String varName = ctx.IDENT().getText();
        Type varType = currentScope.resolve(varName);

        if (varType == null) {
            Token startToken = ctx.getStart();
            errorList.add("Error type 1 at Line " + startToken.getLine() + ": Undefined variable:" + ctx.IDENT().getText() + ".");
        }

        int bracktCnt = ctx.L_BRACKT().size();
        Type currentType = varType;
        for (int i = 0; i < bracktCnt; i++) {
            // Error Type 9: Subscripted value is not an array.
            if (!(currentType instanceof ArrayType)) {
                Token startToken = ctx.getStart();
                errorList.add("Error type 1 at Line " + startToken.getLine() + ": Subscripted value is not an array.");
                break;
            }

            currentType = ((ArrayType)currentType).elementType;
        }

        tmp.type = currentType;

        return tmp;
    }



    // get Expr type.
    @Override public T visitPAREN(SysYParser.PARENContext ctx) {
        return visitChildren(ctx);
    }
    @Override public T visitVarCall(SysYParser.VarCallContext ctx) {
        return visitChildren(ctx);
    }
    @Override public T visitConstInt(SysYParser.ConstIntContext ctx) {
        T tmp = visitChildren(ctx);
        tmp.type = new IntType();
        return tmp;
    }

    public String getType(Type type) {
        if (type instanceof IntType) {
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
