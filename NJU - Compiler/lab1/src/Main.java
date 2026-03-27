import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

import org.antlr.v4.runtime.Token;
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

        sysYLexer.removeErrorListeners();
        ErrorListener myErrorListener = new ErrorListener();
        sysYLexer.addErrorListener(myErrorListener);

        List<? extends Token> myTokens = sysYLexer.getAllTokens();

        if (!myErrorListener.errorMessage.isEmpty()) {
            // 假设myErrorListener有一个错误信息输出函数printLexerErrorInformation.
            myErrorListener.printLexerErrorInformation();
        } else {
            for (Token t : myTokens) {
                int typeId = t.getType();
                String type = sysYLexer.VOCABULARY.getSymbolicName(typeId);
                if (type == "Binary" || type == "Octal" || type == "Hexadecimal" || type == "Decimal") {
                    int value = 0;
                    if (type == "Binary") {
                        value = BinaryToDecimal(t.getText());
                    } else if (type == "Octal") {
                        value = OctalToDecimal(t.getText());
                    } else if (type == "Hexadecimal"){
                        value = HexToDecimal(t.getText());
                    } else {
                        value = StringToInt(t.getText());
                    }
                    
                    System.err.println("INTEGER_CONST" + " " + value + " at Line " + t.getLine() + '.');
                } else {
                    System.err.println(type + " " + t.getText() + " at Line " + t.getLine() + '.');
                }
            }
        }
    }
    
    public static int BinaryToDecimal(String text) {
        int len = text.length();
        int result = 0;
        for (int i = 2; i < len; i++) {
            result = (result << 1) | (text.charAt(i) - '0');
        }

        return result;
    }
    
    public static int OctalToDecimal(String text) {
        int len = text.length();
        int result = 0;
        for (int i = 1; i < len; i++) {
            result = (result * 8) + (text.charAt(i) - '0');
        }

        return result;
    }

    public static int HexToDecimal(String text) {
        int len = text.length();
        int result = 0;
        for (int i = 2; i < len; i++) {
            char cur = text.charAt(i);
            result *= 16;
            if (cur >= '0' && cur <= '9') {
                result += cur - '0';
            } else if (cur >= 'a' && cur <= 'f') {
                result += cur - 'a' + 10;
            } else {
                result += cur - 'A' + 10;
            }
        }

        return result;
    }

    public static int StringToInt(String text) {
        int len = text.length();
        int result = 0;
        for (int i = 0; i < len; i++) {
            char cur = text.charAt(i);
            result = (result * 10) + (cur - '0');
        }

        return result;
    }
    // public static void printSysYTokenInformation(Token t) {
    //     String SymbolicName = sysYLexer.VOCABULARY.getSymbolicName()
    //     System.out.println("type = " + t.getType() + " --- " + t.getText() + " at Line" + t.getLine());
    //     // INT int at Line 1.
    // }
}