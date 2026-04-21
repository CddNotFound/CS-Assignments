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

        }
        
    }
}