import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

// import SysYParser.CodeBlockContext;
// import SysYParser.IfElseContext;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.*;
import java.util.HashMap;
import java.util.ArrayList;

public class BaseVisitor<T> extends SysYParserBaseVisitor<T>{
    HashMap<Integer, TokenStyle> mp;
    // int parenCnt, bracktCnt, braceCnt;
    int braceCnt;
    int level;
    int color;
    boolean underline, blockInBlock, declare, unaryOp;
    boolean firstRow;
    public static final int[] colorList = new int[6];
    int a, b;

    static {
        for (int i = 0; i < 6; i++) {
            colorList[i] = 91 + i;
        }
    }

    public BaseVisitor() {
        mp = new HashMap<Integer, TokenStyle>();
        level = 0;
        braceCnt = 0;
        color = 0;
        underline = false;
        unaryOp = false;
        blockInBlock = false;
        declare = false;
        firstRow = true;
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

        color = 93;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;

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
        return tmp;
    }

    @Override public T visitUnaryOp1(SysYParser.UnaryOp1Context ctx) {
        boolean sUnaryOp = unaryOp;

        unaryOp = true;

        T tmp = visitChildren(ctx);

        unaryOp = sUnaryOp;

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
}
