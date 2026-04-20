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

import org.bytedeco.llvm.LLVM.*;
import org.bytedeco.llvm.program.llvm;

import static org.bytedeco.llvm.global.LLVM.*;

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
        BaseVisitor visitor = new BaseVisitor();
        if (!myErrorListener.errorMessage.isEmpty()) {
            myErrorListener.printLexerErrorInformation();
        } else {
            visitor.visit(tree);
        }
        
        LLVMModuleRef module = visitor.module;
        
        // for (LLVMValueRef globalVar = LLVMGetFirstGlobal(module); globalVar != null; globalVar = LLVMGetNextGlobal(globalVar)) {
            //     System.out.println("  .data");
            //     System.out.println(globalVar.toString());
            
            // }
            
        LLVMValueRef main = LLVMGetFirstFunction(module);

        AsmBuilder writer = new AsmBuilder();
        String funcName = LLVMGetValueName(main).getString();

        writer.buffer.append("  .text\n");
        writer.buffer.append("  .globl " + funcName + "\n");
        writer.prologue(funcName, 0);

        for (LLVMBasicBlockRef block = LLVMGetFirstBasicBlock(main); block != null; block = LLVMGetNextBasicBlock(block)) {
            String blockName = LLVMGetBasicBlockName(block).getString();
            
            writer.buffer.append(blockName + ":\n");
            
            for (LLVMValueRef inst = LLVMGetFirstInstruction(block); inst != null; inst = LLVMGetNextInstruction(inst)) {
                int opcode = LLVMGetInstructionOpcode(inst);
                int operandNum = LLVMGetNumOperands(inst);
                if (operandNum == 2) {
                    LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                    LLVMValueRef op2 = LLVMGetOperand(inst, 1);
                    writer.output(opcode, operandNum, op1, op2);
                } else if (operandNum == 1) {
                    LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                    writer.output(opcode, operandNum, op1, null);
                } else {
                    writer.output(opcode, operandNum, null, null);
                }
            }

        }
        
        writer.epilogue(0);
        System.out.println(writer.buffer);
    }
}