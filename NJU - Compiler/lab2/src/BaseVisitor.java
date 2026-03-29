import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
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
        a = 0;
        b = 0;
    }

    private void printTab() {
        for (int i = 1; i <= level; i++) {
            System.out.println("    ");
        }
    }
    private void println() {
        System.out.println();
    }

    private void printToken(String text, int color, boolean underline) {
        if (color == 0) {
            System.out.print(text);
        } else if (underline && !text.trim().isEmpty()){
                System.out.print("\033[" + color + ";4m" + text + "\033[0m");
        } else {
            System.out.print("\033[" + color + "m" + text + "\033[0m");
        }
    }

    private void printPreviousSpace(int type) {
        if ((type >= 12 && type <= 21 || type >= 23 || type <= 26)
         || ((type == 10 || type == 11) && !unaryOp)) {
            System.out.println(" ");
        }
    }

    private void printFollowingSpace(int type) {
        if ((type >= 1 && type <= 3 || type == 7 || type == 8 || type == 4 || type == 6)
         || (type >= 12 && type <= 21 || type >= 23 || type <= 26)
         || ((type == 10 || type == 11) && !unaryOp)
         || type == 27) {
            System.out.print(" ");
        }
    }


    @Override public T visitProg(SysYParser.ProgContext ctx) {
        // Vocabulary voc = SysYParser.VOCABULARY;

        Interval interval = ctx.getSourceInterval();
        a = interval.a;
        b = interval.b;
        // // String str = ctx.getText();
        // System.out.println("Prog : " + interval.a + " : " + interval.b);
        // for (int i = interval.a; i <= interval.b; i++) {
        //     System.out.println(i + " : " + voc.getSymbolicName(i));
        // }
        return visitChildren(ctx); 
    }

    @Override public T visitTerminal(TerminalNode node) {
        Token token = node.getSymbol();
        int type = token.getType();
        String text = token.getText();
        int idx = token.getTokenIndex();

        int sColor = color;
        boolean sUnderline = underline;

        TokenStyle style = mp.getOrDefault(idx, new TokenStyle());
        if (type >= 1 && type <= 9) { // key word : Bright Cyan
            style.color = 96;
        } else if (type >= 10 && type <= 28) { // operator : Bright Red
            style.color = 91;
        } else if (type == 29) { // const int : Magenta
            style.color = 35;
        } else if (type == 37 || type == 35 || type == 39) {
            style.color = colorList[braceCnt % 6];
            ++braceCnt;
        } else if (type == 38 || type == 36 || type == 40) {
            --braceCnt;
            style.color = colorList[braceCnt % 6];
        }

        printPreviousSpace(type);

        if (declare) {
            printToken(text, color, underline);
        } else if (type == 37) {
            if (blockInBlock) {
                printTab();
                printToken(text, color, underline);
                println();
                level += 1;
            } else {
                System.out.print(" ");
                printToken(text, color, underline);
                println();
                level += 1;
            }
        } else if (type == 38) {
            level -= 1;
            printTab();
            printToken(text, color, underline);
            println();
        }

        printFollowingSpace(type);

        color = sColor;
        underline = sUnderline;

        return (T)this.defaultResult();
    }

    @Override public T visitCodeBlock(SysYParser.CodeBlockContext ctx) {
    
        ParseTree parent = ctx.getParent();
        if (parent instanceof SysYParser.CodeBlockContext) {
            blockInBlock = true;
        } else {
            blockInBlock = false;
        }

        T tmp = visitChildren(ctx);
    
        return tmp;
    }

    @Override public T visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
        println();
        printTab();
        int sColor = color;
        boolean sUnderline = underline;

        color = 93;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;

        println();
        
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

        println();
        
        return tmp;
    }

    @Override public T visitIfElse(SysYParser.IfElseContext ctx) {
        ParseTree parent = ctx.getParent();
        if ((parent instanceof SysYParser.IfElseContext)
         || parent instanceof SysYParser.WhileLoopContext) {
            SysYParser.IfElseContext ftr = (SysYParser.IfElseContext) ctx.getParent();
            if (ftr.stat().size() > 1 && ftr.stat(1) == ctx) {
                System.out.print(" ");
            } else {
                println();
                level += 1;
                printTab();
                level -= 1;
            }
        } else {
            printTab();
        }
        int sColor = color;
        boolean sUnderline = underline;

        color = 97;
        underline = false;

        T tmp = visitChildren(ctx);

        color = sColor;
        underline = sUnderline;

        println();
        
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

        println();
        
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

        println();
        
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

        println();
        
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
            level -= 1;
        } else {
            printTab();
        }
    }
}
