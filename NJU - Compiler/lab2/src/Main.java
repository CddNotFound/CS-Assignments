import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.TreeWizard.Visitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
// import sysYParser;
// import .antlr.SysYLexer;

public class Main
{    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);

        // sysYLexer.removeErrorListeners();
        ErrorListener myErrorListener = new ErrorListener();
        // sysYLexer.addErrorListener(myErrorListener);
        
        CommonTokenStream tokens = new CommonTokenStream(sysYLexer);
        
        // int idx = 0;
        // List<? extends Token> myTokens = sysYLexer.getAllTokens();
        // for (Token t : myTokens) {
        //     System.out.println(idx + " : " + t.getText());
        //     ++idx;
        // }
        


        SysYParser sysYParser = new SysYParser(tokens);
        
        sysYParser.removeErrorListeners();
        sysYParser.addErrorListener(myErrorListener);

        ParseTree tree = sysYParser.prog();
        if (!myErrorListener.errorMessage.isEmpty()) {
            myErrorListener.printLexerErrorInformation();
        } else {
            BaseVisitor visitor = new BaseVisitor();
            visitor.visit(tree);        

            for (int i = visitor.a; i < visitor.b; i++) {
                Token token = tokens.get(i);
                int idx = token.getTokenIndex();
                String text = token.getText();
                TokenStyle style = (TokenStyle) visitor.mp.getOrDefault(idx, null);
                if (style == null) {
                    System.out.print(text);
                } else if (style.underline && !text.trim().isEmpty()){
                    System.out.print("\033[" + style.color + ";4m" + text + "\033[0m");
                } else {
                    System.out.print("\033[" + style.color + "m" + text + "\033[0m");
                }
            }
        }
        
    }
    
    // public static int BinaryToDecimal(String text) {
    //     int len = text.length();
    //     int result = 0;
    //     for (int i = 2; i < len; i++) {
    //         result = (result << 1) | (text.charAt(i) - '0');
    //     }

    //     return result;
    // }
    
    // public static int OctalToDecimal(String text) {
    //     int len = text.length();
    //     int result = 0;
    //     for (int i = 1; i < len; i++) {
    //         result = (result * 8) + (text.charAt(i) - '0');
    //     }

    //     return result;
    // }

    // public static int HexToDecimal(String text) {
    //     int len = text.length();
    //     int result = 0;
    //     for (int i = 2; i < len; i++) {
    //         char cur = text.charAt(i);
    //         result *= 16;
    //         if (cur >= '0' && cur <= '9') {
    //             result += cur - '0';
    //         } else if (cur >= 'a' && cur <= 'f') {
    //             result += cur - 'a' + 10;
    //         } else {
    //             result += cur - 'A' + 10;
    //         }
    //     }

    //     return result;
    // }

    // public static int StringToInt(String text) {
    //     int len = text.length();
    //     int result = 0;
    //     for (int i = 0; i < len; i++) {
    //         char cur = text.charAt(i);
    //         result = (result * 10) + (cur - '0');
    //     }

    //     return result;
    // }
    // public static void printSysYTokenInformation(Token t) {
    //     String SymbolicName = sysYLexer.VOCABULARY.getSymbolicName()
    //     System.out.println("type = " + t.getType() + " --- " + t.getText() + " at Line" + t.getLine());
    //     // INT int at Line 1.
    // }
}