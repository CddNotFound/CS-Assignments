// Generated from ./src/SysYLexer.g4 by ANTLR 4.9.1
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;
import java.util.ArrayList;;

// public class ErrorListener extends BaseErrorListener{
//     ArrayList<String> errorMessage;
//     public ErrorListener() {
//         errorMessage = new ArrayList<String>();
//     }

//     public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
//         errorMessage.add("Error type A at Line " + line + ": " + msg + ".");
//         // Error type A at Line [lineNo]:[errorMessage]
//    }
//    public void printLexerErrorInformation() {
//         for (String message : errorMessage) {
//             System.err.println(message);
//         }
//    }
// }

public class ErrorListener extends BaseErrorListener{
    ArrayList<String> errorMessage;
    public ErrorListener() {
        errorMessage = new ArrayList<String>();
    }

    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // Token token = (Token) offendingSymbol;
        errorMessage.add("Error type B at Line " + line + ": " + msg + ".");
        // Error type A at Line [lineNo]:[errorMessage]
   }
   public void printLexerErrorInformation() {
        for (String message : errorMessage) {
            System.out.println(message);
        }
   }
}
