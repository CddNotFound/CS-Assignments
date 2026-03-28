import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.*;
import java.util.HashMap;
import java.util.ArrayList;

public class BaseVisitor<T> extends SysYParserBaseVisitor<T>{
    HashMap<Integer, TokenStyle> mp;
    // int parenCnt, bracktCnt, braceCnt;
    int braceCnt;
    public static final int[] colorList = new int[6];
    int a, b;

    static {
        for (int i = 0; i < 6; i++) {
            colorList[i] = 91 + i;
        }
    }

    public BaseVisitor() {
        mp = new HashMap<Integer, TokenStyle>();
        // parenCnt = 0;
        braceCnt = 0;
        // braceCnt = 0;
        a = 0;
        b = 0;
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

        TokenStyle style = mp.getOrDefault(idx, new TokenStyle());
        if (type >= 1 && type <= 9) { // key word : Bright Cyan
            // System.out.print("Keyword : " + text + "\n");
            style.color = 96;
        } else if (type >= 10 && type <= 28) { // operator : Bright Red
            // System.out.print("Operator : " + text + "\n");
            style.color = 91;
        } else if (type == 29) { // const int : Magenta
            // System.out.print("Const int : " + text + "\n");
            style.color = 35;
        // } else if (type == 35) {
        //     style.color = colorList[parenCnt % 6];
        //     ++parenCnt;
        // } else if (type == 36) {
        //     --parenCnt;
        //     style.color = colorList[parenCnt % 6];
        } else if (type == 37 || type == 35 || type == 39) {
            style.color = colorList[braceCnt % 6];
            ++braceCnt;
        } else if (type == 38 || type == 36 || type == 40) {
            --braceCnt;
            style.color = colorList[braceCnt % 6];
        } 
        // else if (type == 39) {
        //     style.color = colorList[bracktCnt % 6];
        //     ++bracktCnt;
        // } else if (type == 40) {
        //     --bracktCnt;
        //     style.color = colorList[bracktCnt % 6];
        // }
        mp.put(idx, style);
        // System.out.println("Terminal : Tpye = " + token.getType() + " = "  + node.getText());

        return (T)this.defaultResult();
    }

    @Override public T visitFunctionDecl(SysYParser.FunctionDeclContext ctx) {
        String text = ctx.funcDecl().IDENT().getText();
        int idx = ctx.funcDecl().IDENT().getSourceInterval().a;

        TokenStyle style = mp.getOrDefault(idx, new TokenStyle());
        style.color = 93;
        mp.put(idx, style);
        // System.out.print("FunctionDecl : " + text + "\n");

        Interval interval = ctx.getSourceInterval();
        // System.out.println("varDecl token interval : [" + interval.a + ", " + interval.b + "]");

        // setDefualtColor(interval.a, interval.b, 95);
        // modifyUnderline(interval.a, interval.b, true);

        return visitChildren(ctx); 
    }
    
    @Override public T visitFunctionCall(SysYParser.FunctionCallContext ctx) {
        String text = ctx.IDENT().getText();
        int idx = ctx.IDENT().getSourceInterval().a;

        TokenStyle style = mp.getOrDefault(idx, new TokenStyle());
        style.color = 93;
        mp.put(idx, style);

        // System.out.print("FunctionCall : " + text + "\n");
        
    
        return visitChildren(ctx);
    }

	@Override public T visitVariableDecl(SysYParser.VariableDeclContext ctx) {
        Interval interval = ctx.getSourceInterval();
        // System.out.println("varDecl token interval : [" + interval.a + ", " + interval.b + "]");

        setDefualtColor(interval.a, interval.b, 95);
        modifyUnderline(interval.a, interval.b, true);
        // TokenStyle style = mp.getOrDefault(idx, new TokenStyle());
        // mp.put(idx, style);
        
        return visitChildren(ctx);
    }

    @Override public T visitIfElse(SysYParser.IfElseContext ctx) {
        Interval interval = ctx.getSourceInterval();
        // System.out.println("varDecl token interval : [" + interval.a + ", " + interval.b + "]");

        setDefualtColor(interval.a, interval.b, 97);
        return visitChildren(ctx);
    }
	@Override public T visitReturn(SysYParser.ReturnContext ctx) {
        Interval interval = ctx.getSourceInterval();
        // System.out.println("varDecl token interval : [" + interval.a + ", " + interval.b + "]");

        setDefualtColor(interval.a, interval.b, 97);

        return visitChildren(ctx);
    }
	@Override public T visitExpd(SysYParser.ExpdContext ctx) {
        Interval interval = ctx.getSourceInterval();
        // System.out.println("varDecl token interval : [" + interval.a + ", " + interval.b + "]");

        setDefualtColor(interval.a, interval.b, 97);
        
        return visitChildren(ctx);
    }

    public void modifyColor(int a, int b, int color) {
        for (int i = a; i <= b; i++) {
            TokenStyle style = mp.getOrDefault(i, new TokenStyle());
            style.color = color;
            mp.put(i, style);
        }
    }
    public void modifyUnderline(int a, int b, boolean underline) {
        for (int i = a; i <= b; i++) {
            TokenStyle style = mp.getOrDefault(i, new TokenStyle());
            style.underline = underline;
            mp.put(i, style);
        }
    }
    public void setDefualtColor(int a, int b, int color) {
        for (int i = a; i <= b; i++) {
            TokenStyle style = mp.getOrDefault(i, new TokenStyle(color, false));
            mp.put(i, style);
        }
    }
}
