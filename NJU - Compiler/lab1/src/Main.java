import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import .antlr.SysYLexer;

public class Main
{    
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("input path is required");
        }
        String source = args[0];
        CharStream input = CharStreams.fromFileName(source);
        SysYLexer sysYLexer = new SysYLexer(input);
    }
    
    // public static void main(String[] args) {
    //     if (args.length < 1) {
    //         return ;
    //     }

    //     String file = args[0];
    //     try (FileReader fr = new FileReader(file)) {
    //         BufferedReader br = new BufferedReader(fr);


            
    //         String str = br.readLine();
    //         if (str == null) {
    //             return ;
    //         }
    //         System.out.print(str);
    //         while ((str = br.readLine()) != null) {
    //             System.out.println();
    //             System.out.print(str);
    //         }
    //     } catch (IOException e) {
    //         System.out.println(e.getMessage());
    //     }
    // }
}