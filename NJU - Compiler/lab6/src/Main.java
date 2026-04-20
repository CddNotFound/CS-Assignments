import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        AsmBuilder writer = new AsmBuilder();
        
        int spOffset = 0;
        
        LLVMValueRef main = LLVMGetFirstFunction(module);
        String funcName = LLVMGetValueName(main).getString();

        for (LLVMValueRef globalVar = LLVMGetFirstGlobal(module); globalVar != null; globalVar = LLVMGetNextGlobal(globalVar)) {
            String varName = LLVMGetValueName(globalVar).getString();        
            LLVMValueRef initializer = LLVMGetInitializer(globalVar);
            long value = LLVMConstIntGetSExtValue(initializer);

            writer.buffer.append("  .data\n");
            writer.buffer.append(varName + ":\n");
            writer.buffer.append("  .word " + value + "\n\n");

            writer.globalVar.add(varName);
        }

        for (LLVMBasicBlockRef block = LLVMGetFirstBasicBlock(main); block != null; block = LLVMGetNextBasicBlock(block)) {
            
            for (LLVMValueRef inst = LLVMGetFirstInstruction(block); inst != null; inst = LLVMGetNextInstruction(inst)) {
                // int opcode = LLVMGetInstructionOpcode(inst);

                String destName = LLVMGetValueName(inst).getString();
                if (!destName.equals("")) {
                    if (writer.offsetMp.getOrDefault(destName, null) == null) {
                        spOffset -= 4;
                        // System.out.println("put " + destName);
                        writer.offsetMp.put(destName, spOffset);
                    }
                }

                int len = LLVMGetNumOperands(inst);
                for (int i = 0; i < len; i++) {
                    LLVMValueRef v = LLVMGetOperand(inst, 0);
                    String varName = LLVMGetValueName(v).getString();
                    if (varName.equals("") || writer.offsetMp.getOrDefault(varName, null) != null) {
                        continue;
                    }
                    
                        // System.out.println("put " + varName);
                    spOffset -= 4;
                    writer.offsetMp.put(varName, spOffset);
                }
            }
        }
        // System.out.println("offset = " + spOffset);
            
        spOffset = (spOffset - 15) / 16 * 16;

        for (Map.Entry<String, Integer> entry : writer.offsetMp.entrySet()) {
            entry.setValue(entry.getValue() - spOffset);
        }

        writer.buffer.append("  .text\n");
        writer.buffer.append("  .globl " + funcName + "\n");
        writer.prologue(funcName, spOffset);

        for (LLVMBasicBlockRef block = LLVMGetFirstBasicBlock(main); block != null; block = LLVMGetNextBasicBlock(block)) {
            String blockName = LLVMGetBasicBlockName(block).getString();
            
            writer.buffer.append(blockName + ":\n");
            
            for (LLVMValueRef inst = LLVMGetFirstInstruction(block); inst != null; inst = LLVMGetNextInstruction(inst)) {
                int opcode = LLVMGetInstructionOpcode(inst);

                if (opcode == 27) {
                    LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                    writer.load(op1, inst, "sp");
                } else if (opcode == 28) {
                    LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                    LLVMValueRef op2 = LLVMGetOperand(inst, 1);
                    
                    writer.store(op1, op2, "sp");
                } else {
                    if (opcode == 26) {
                        continue;
                    }
                    int operandNum = LLVMGetNumOperands(inst);
                    if (operandNum == 2) {
                        LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                        LLVMValueRef op2 = LLVMGetOperand(inst, 1);
                        writer.output(opcode, operandNum, inst, op1, op2);
                    } else if (operandNum == 1) {
                        LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                        writer.output(opcode, operandNum, inst, op1, null);
                    } else {
                        writer.output(opcode, operandNum, inst, null, null);
                    }
                }

            }

        }
        
        writer.epilogue(-spOffset);
        System.out.println(writer.buffer);
    }
}