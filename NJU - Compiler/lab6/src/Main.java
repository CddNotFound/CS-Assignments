import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.HashSet;
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

import java.util.Comparator;


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
        LiveInterval interval = new LiveInterval();
        
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

        int idx = 0;
        for (LLVMBasicBlockRef block = LLVMGetFirstBasicBlock(main); block != null; block = LLVMGetNextBasicBlock(block)) {
            
            for (LLVMValueRef inst = LLVMGetFirstInstruction(block); inst != null; inst = LLVMGetNextInstruction(inst)) {
                idx += 1;
                // int opcode = LLVMGetInstructionOpcode(inst);

                String destName = LLVMGetValueName(inst).getString();
                if (!destName.equals("")) {
                    // calculate sp
                    if (writer.variableMp.getOrDefault(destName, null) == null) {
                    //     spOffset -= 4;
                        writer.variableMp.put(destName, spOffset);
                    }

                    // get live interval
                    interval.push(destName, idx);
                }

                int len = LLVMGetNumOperands(inst);
                for (int i = 0; i < len; i++) {
                    LLVMValueRef v = LLVMGetOperand(inst, i);
                    String varName = LLVMGetValueName(v).getString();
                    if (!varName.equals("")) {
                        // calculate sp
                        if (writer.variableMp.getOrDefault(varName, null) == null) {
                        //     spOffset -= 4;
                            writer.variableMp.put(varName, spOffset);
                        }
                        // get live interval
                        interval.push(varName, idx);
                    }
                }
            }
        }

        ArrayList<IntervalInfo> list = new ArrayList<IntervalInfo>();
        for (Map.Entry<String, Integer> entry : writer.variableMp.entrySet()) {
            String varName = entry.getKey();
            int st = interval.getInterval(varName).getKey();
            int ed = interval.getInterval(varName).getValue();
            list.add(new IntervalInfo(varName, st, ed));
        }

        // live-variable analysis
        list.sort(Comparator.comparing((IntervalInfo info) -> info.beginIdx)
                            .thenComparing((IntervalInfo info) -> info.endIdx));
        TreeMap<Integer, ArrayList<IntervalInfo>> release = new TreeMap<Integer, ArrayList<IntervalInfo>>();
        HashSet<Integer> unused = new HashSet<Integer>();
        for (int i = 0; i <= 21; i++) unused.add(i);
        for (IntervalInfo tmp : list) {
            String varName = tmp.name;
            int st = tmp.beginIdx;
            int ed = tmp.endIdx;

            if (writer.globalVar.contains(varName)) {
                // global var
                writer.registerId.put(varName, 22);
                continue;
            }

            while (!release.isEmpty() && release.firstEntry().getKey() <= st) {
                // release variables, which are out of date
                int edTime = release.firstEntry().getKey();
                for (IntervalInfo reg :  release.firstEntry().getValue()) {
                    int id = writer.registerId.get(reg.name);
                    unused.add(id);
                }
                release.remove(edTime);
            }

            while (unused.isEmpty() && release.lastKey() >= ed) {
                // remove latest interval
                Map.Entry<Integer, ArrayList<IntervalInfo>> lst = release.lastEntry();
                IntervalInfo reg = lst.getValue().get(0);
                lst.getValue().remove(0);
                
                String lastName = reg.name;
                int lastId = writer.registerId.get(lastName);
                if (lastId < 22) {
                    unused.add(lastId);
                    writer.registerId.put(lastName, 22);
                }

                if (lst.getValue().size() == 0) {
                    release.remove(lst.getKey());
                }
            }

            if (unused.isEmpty()) {  // stack
                writer.registerId.put(varName, 22);
            } else {
                int id = unused.iterator().next();
                unused.remove(id);
                writer.registerId.put(varName, id);

                // when to release register
                release.computeIfAbsent(ed, k -> new ArrayList<IntervalInfo>()).add(tmp);
            }
        }

        // calculate stack position
        for (IntervalInfo tmp : list) {
            if (writer.registerId.get(tmp.name) == 22) {
                String varName = tmp.name;
                if (!writer.globalVar.contains(tmp.name) && writer.offsetMp.getOrDefault(varName, null) == null) {
                    spOffset -= 4;
                    writer.offsetMp.put(varName, spOffset);
                }
            }
        }

        spOffset = (spOffset - 15) / 16 * 16;
        for (Map.Entry<String, Integer> entry : writer.offsetMp.entrySet()) {
            entry.setValue(entry.getValue() - spOffset);
        }

        // generate RISC-V code
        writer.buffer.append("  .text\n");
        writer.buffer.append("  .globl " + funcName + "\n");
        writer.prologue(funcName, spOffset);

        for (LLVMBasicBlockRef block = LLVMGetFirstBasicBlock(main); block != null; block = LLVMGetNextBasicBlock(block)) {
            String blockName = LLVMGetBasicBlockName(block).getString();
            
            writer.buffer.append(blockName + ":\n");
            
            for (LLVMValueRef inst = LLVMGetFirstInstruction(block); inst != null; inst = LLVMGetNextInstruction(inst)) {
                int opcode = LLVMGetInstructionOpcode(inst);

                if (opcode == LLVMLoad) {  // load
                    LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                    String nameDest = LLVMGetValueName(inst).getString();
                    String nameSource = LLVMGetValueName(op1).getString();
                    if (!nameDest.equals("") && writer.registerId.get(nameDest) == 22 && writer.globalVar.contains(nameDest) && nameDest.equals(nameSource)) {
                        // if variable is global and doesn't own a register.
                        // straightly load from global.
                        continue;
                    }

                    writer.load(op1, inst, "sp");
                } else if (opcode == LLVMStore) {  // store
                    LLVMValueRef op1 = LLVMGetOperand(inst, 0);
                    LLVMValueRef op2 = LLVMGetOperand(inst, 1);
                    
                    writer.store(op1, op2, "sp");
                } else {
                    if (opcode == LLVMAlloca) { // alloca
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