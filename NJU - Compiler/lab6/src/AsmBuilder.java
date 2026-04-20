import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    StringBuffer buffer;
    HashMap<String, Integer> offsetMp;
    HashSet<String> globalVar;
    static HashMap<Integer, String> opt;

    static {
        opt = new HashMap<Integer, String>();
        opt.put(8, "add");
        opt.put(10, "sub");
        opt.put(12, "mul");
        opt.put(15, "sdiv");
        opt.put(18, "srem");
    }

    public AsmBuilder() {
        buffer = new StringBuffer();
        offsetMp = new HashMap<String, Integer>();
        globalVar = new HashSet<String>();
    }

    public void get(LLVMValueRef var, String dest) {
        String name = LLVMGetValueName(var).getString();
        if (name.equals("")) { // const int
            op1("li", dest, "" + LLVMConstIntGetSExtValue(var));
        } else if (globalVar.contains(name)) { // global var
            op1("la", "t0", name);
            buffer.append(String.format("  lw %s, 0(t0)\n", dest));
            // globalVar.remove(name);
        } else {
            // buffer.append(("var = " + name + "\n"));
            buffer.append(String.format("  lw %s, %d(sp)\n", dest, offsetMp.get(name)));
        }
    }
    public void push(String source, String dest) {
        if (globalVar.contains(dest)) { // global var
            op1("la", "t1", dest);
            buffer.append(String.format("  sw %s, 0(t1)\n", source));
            // globalVar.remove(name);
        } else {
            // buffer.append(("var = " + name + "\n"));
            buffer.append(String.format("  sw %s, %d(sp)\n", source, offsetMp.get(dest)));
        }
    }

    public void output(int opType, int opNum, LLVMValueRef inst, LLVMValueRef op1, LLVMValueRef op2) {
        // System.out.println("opType" + "= " + opType);
        if (opType == 1) { // ret -> call exit
            String name = LLVMGetValueName(op1).getString();
            get(op1, "a0");
        } else if (opType == 8 || opType == 10 || opType == 12 || opType == 15 || opType == 18) {
            // add, sub, mul, sdiv, srem
            // load op1 -> t1
            // load op2 -> t2
            // add t0, t1, t2
            // store t0 -> op1
            // int offset1 = offsetMp.get(LLVMGetValueName(op2))
            String nameDest = LLVMGetValueName(inst).getString();
            // String name1 = LLVMGetValueName(op1).getString();
            // String name2 = LLVMGetValueName(op2).getString();
            get(op1, "t1");
            get(op2, "t2");
            // if (name1.equals("") || name2.equals("")) {
            //     if (name1.equals("")) {
            //         op2("addi", "t0", "t0", "" + LLVMConstIntGetSExtValue(op1));
            //         buffer.append(String.format("  lw t0, %d(sp)\n", offsetMp.get(name2)));
            //     } else {
            //         buffer.append(String.format("  lw t1, %d(sp)\n", offsetMp.get(name2)));
            //         op2("addi", "t1", "t1", "" + LLVMConstIntGetSExtValue(op2));
            //     }
            // } else {
            //     buffer.append(String.format("  lw t0, %d(sp)\n", offsetMp.get(name2)));
            //     buffer.append(String.format("  lw t1, %d(sp)\n", offsetMp.get(name2)));
            // }
            op2(opt.get(opType), "t0", "t1", "t2");
            push("t0", nameDest);
        }
    }

    public void op2(String op, String dest, String lhs, String rhs) {
        buffer.append(String.format("  %s %s, %s, %s\n", op, dest, lhs, rhs));
    }
    public void op1(String op, String dest, String lhs) {
        buffer.append(String.format("  %s %s, %s\n", op, dest, lhs));
    }
    public void op0(String op) {
        buffer.append(String.format("  %s\n", op));
    }
    public void load(LLVMValueRef op1, LLVMValueRef op2, String lhs) {
        // String source = LLVMGetValueName(op1).getString();
        String dest = LLVMGetValueName(op2).getString();
        get(op1, "t0");

        String source = LLVMGetValueName(op1).getString();
        // buffer.append(source + " -> " + dest);
        if (source.equals(dest)) {
            buffer.append(String.format("  sw %s, %d(%s)\n", "t0", offsetMp.get(dest), lhs));
        } else {
            push("t0", dest);
        }
        // buffer.append(String.format("  lw %s, %d(%s)\n", "t0", offsetMp.get(source), lhs));
        // buffer.append(dest);
    }
    public void store(LLVMValueRef op1, LLVMValueRef op2, String lhs) {
        // String source = LLVMGetValueName(op1).getString();
        String dest = LLVMGetValueName(op2).getString();
        // System.out.println("store " + source + " to " + dest);
        get(op1, "t0");
        push("t0", dest);
        // buffer.append(String.format("  sw %s, %d(%s)\n", "t0", offsetMp.get(dest), lhs));
    }


    public void prologue(String name, int num) {
        buffer.append(name + ":\n");
        op2("addi", "sp", "sp", "" + num);
    }
    public void epilogue(int num) {
        op2("addi", "sp", "sp", "" + num);
        op1("li", "a7", "93");
        op0("ecall");
    }
}
