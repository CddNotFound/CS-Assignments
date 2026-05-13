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
import java.util.Collections;

public class BaseVisitor<T> extends SysYParserBaseVisitor<T>{
    HashMap<Integer, TokenStyle> mp;
    // int parenCnt, bracktCnt, braceCnt;
    int braceCnt;
    int level;
    boolean blockInBlock, unaryOp;
    boolean firstRow;
    ArrayList<Integer> color;
    ArrayList<Boolean> declare, underline;
    int colorCnt, delcareCnt, underlineCnt;
    public static final int[] colorList = new int[6];
    int a, b;
    boolean singleRet;

    static {
        for (int i = 0; i < 6; i++) {
            colorList[i] = 91 + i;
        }
    }

    public BaseVisitor() {
        mp = new HashMap<Integer, TokenStyle>();
        level = 0;
        braceCnt = 0;
        unaryOp = false;
        blockInBlock = false;
        firstRow = true;
        a = 0;
        b = 0;
        colorCnt = 1; delcareCnt = 1; underlineCnt = 1;
        color = new ArrayList<Integer>(Collections.nCopies(1000, 0));
        declare = new ArrayList<Boolean>(Collections.nCopies(1000, false));
        underline = new ArrayList<Boolean>(Collections.nCopies(1000, false));
        color.set(1, 0);
        declare.set(1, false);
        underline.set(1, false);
        singleRet = false;
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
        if (text.equals("<EOF>")) {
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
        if (((type >= 1 && type <= 3) || type == 7 || type == 4 || (type == 6 && !singleRet))
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
    // @Override public T visitLVal(SysYParser.LValContext ctx) {
    //     visitChildren(ctx);
    //     return null;
    // }

    @Override public T visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        int type = token.getType();
        String text = token.getText();
        int idx = token.getTokenIndex();

        if ((type >= 1 && type <= 29) || (type >= 35 && type <= 40)) {
            ++colorCnt;
        }

        if (type >= 1 && type <= 9) { // key word : Bright Cyan
            color.set(colorCnt, 96);
        } else if (type >= 10 && type <= 28) { // operator : Bright Red
            color.set(colorCnt, 91);
        } else if (type == 29) { // const int : Magenta
            color.set(colorCnt, 35);
        } else if (type == 37 || type == 35 || type == 39) {
            color.set(colorCnt, colorList[braceCnt % 6]);
            ++braceCnt;
        } else if (type == 38 || type == 36 || type == 40) {
            --braceCnt;
            color.set(colorCnt, colorList[braceCnt % 6]);
        }

        printPreviousSpace(type);

        if (declare.get(delcareCnt)) {
            // System.err.print("delcare: ");
            printToken(text, color.get(colorCnt), underline.get(underlineCnt));
        } else if (type == 37) {
            // System.err.print("L_BRACE: ");
            if (blockInBlock) {
                println();
                printTab();
                printToken(text, color.get(colorCnt), underline.get(underlineCnt));
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
                printToken(text, color.get(colorCnt), underline.get(underlineCnt));
                level += 1;
            }
        } else if (type == 38) {
            // System.err.print("R_BRACE: ");
            println();
            level -= 1;
            printTab();
            printToken(text, color.get(colorCnt), underline.get(underlineCnt));
        } else if (type == 8) {
            SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) node.getParent();
            if (!(ftr.stat(0) instanceof SysYParser.CodeBlockContext)) {
                // level -= 1;
            }
            println();
            printTab();
            printToken(text, color.get(colorCnt), underline.get(underlineCnt));
        } else {
            printToken(text, color.get(colorCnt), underline.get(underlineCnt));
        }

        printFollowingSpace(type);

        if ((type >= 1 && type <= 29) || (type >= 35 && type <= 40)) {
            --colorCnt;
        }

        return (T)this.defaultResult();
    }

    @Override public T visitBreak(SysYParser.BreakContext ctx) {
        stmtTab(ctx.getParent());
        T tmp = visitChildren(ctx);
        exitStmt(ctx.getParent());
        return null;
    }
    @Override public T visitEmpty(SysYParser.EmptyContext ctx) {
        stmtTab(ctx.getParent());
        
        ++colorCnt;
        ++underlineCnt;

        color.set(colorCnt, 0);
        underline.set(underlineCnt, false);

        T tmp = visitChildren(ctx);

        --colorCnt;
        --underlineCnt;

        exitStmt(ctx.getParent());

        return tmp;
    }
    @Override public T visitContinue(SysYParser.ContinueContext ctx) {
        stmtTab(ctx.getParent());
        T tmp = visitChildren(ctx);
        exitStmt(ctx.getParent());
        return null;
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

        visit(ctx.funcDecl().allType());

        ++colorCnt;

        color.set(colorCnt, 93);
        visit(ctx.funcDecl().IDENT());
        --colorCnt;

        visit(ctx.funcDecl().L_PAREN());
        if (ctx.funcDecl().parameters() != null) {
            visit(ctx.funcDecl().parameters());
        }
        visit(ctx.funcDecl().R_PAREN());
        visit(ctx.funcDecl().block());
        
        return null;
    }

    @Override public T visitParameters(SysYParser.ParametersContext ctx) {
        // ++colorCnt;

        // color.set(colorCnt, 0);

        T tmp = visitChildren(ctx);

        // --colorCnt;
        
        return tmp;
    }
    
    @Override public T visitFunctionCall(SysYParser.FunctionCallContext ctx) {
        ++colorCnt;

        color.set(colorCnt, 93);

        visit(ctx.IDENT());

        --colorCnt;

        visit(ctx.L_PAREN());
        if (ctx.funcRParams() != null) {
            visit(ctx.funcRParams());
        }
        visit(ctx.R_PAREN());


        return null;
    }

	@Override public T visitVariableDecl(SysYParser.VariableDeclContext ctx) {    
        stmtTab(ctx.getParent());
        ++colorCnt;
        ++underlineCnt;
        ++delcareCnt;

        color.set(colorCnt, 95);
        underline.set(underlineCnt, true);
        declare.set(delcareCnt, true);

        T tmp = visitChildren(ctx);

        --colorCnt;
        --underlineCnt;
        --delcareCnt;

        exitStmt(ctx.getParent());
        return tmp;
    }

    @Override public T visitWhileLoop(SysYParser.WhileLoopContext ctx) {
        stmtTab(ctx.getParent());
        ++colorCnt;
        ++underlineCnt;

        color.set(colorCnt, 97);
        underline.set(underlineCnt, false);

        T tmp = visitChildren(ctx);

        --colorCnt;
        --underlineCnt;

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
        
        ++colorCnt;
        ++underlineCnt;

        color.set(colorCnt, 97);
        underline.set(underlineCnt, false);

        visit(ctx.IF());
        visit(ctx.L_PAREN());
        visit(ctx.cond());
        visit(ctx.R_PAREN());
        visit(ctx.stat(0));

        if (ctx.ELSE() != null) {
            // println();
            // printTab();
            visit(ctx.ELSE());

            if (ctx.stat(1) instanceof SysYParser.IfElseContext
            || ctx.stat(1) instanceof SysYParser.CodeBlockContext) {
                System.out.print(" ");
            }

            visit(ctx.stat(1));
        }

        --colorCnt;
        --underlineCnt;

        if (parent instanceof SysYParser.IfElseContext) {
            SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) parent;
            if (ftr.stat().size() > 1 && ftr.stat(1) == ctx) {
                // System.out.print(" ");
            } else {
                level -= 1;
            }
        }
        // println();
        
        return null;
    }

	@Override public T visitReturn(SysYParser.ReturnContext ctx) {
        stmtTab(ctx.getParent());
        ++colorCnt;
        ++underlineCnt;

        color.set(colorCnt, 97);
        underline.set(underlineCnt, false);
        
        if (ctx.exp() == null) { singleRet = true;}

        T tmp = visitChildren(ctx);

        singleRet = false;

        --colorCnt;
        --underlineCnt;
        
        exitStmt(ctx.getParent());
        return tmp;
    }
    
	@Override public T visitExpd(SysYParser.ExpdContext ctx) {
        stmtTab(ctx.getParent());
        ++colorCnt;
        ++underlineCnt;

        color.set(colorCnt, 97);
        underline.set(underlineCnt, false);

        T tmp = visitChildren(ctx);

        --colorCnt;
        --underlineCnt;
        
        exitStmt(ctx.getParent());
        return tmp;
    }

    @Override public T visitVarAssign(SysYParser.VarAssignContext ctx) {
        stmtTab(ctx.getParent());
        ++colorCnt;
        ++underlineCnt;

        color.set(colorCnt, 97);
        underline.set(underlineCnt, false);

        T tmp = visitChildren(ctx);

        --colorCnt;
        --underlineCnt;
        
        exitStmt(ctx.getParent());
        return tmp;
    }

    @Override public T visitUnaryOp1(SysYParser.UnaryOp1Context ctx) {
        boolean sUnaryOp = unaryOp;

        unaryOp = true;

        visit(ctx.unaryOp());

        unaryOp = sUnaryOp;

        visit(ctx.exp());

        return null;
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
