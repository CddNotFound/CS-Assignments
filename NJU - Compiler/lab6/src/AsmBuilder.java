import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    StringBuffer buffer;
    HashMap<String, Integer> offsetMp, variableMp;
    HashSet<String> globalVar;
    HashMap<String, Integer> registerId;
    static HashMap<Integer, String> opt;
    static ArrayList<String> registerInfo;

    String t0, t1, t2;

    static {
        opt = new HashMap<Integer, String>();
        opt.put(8, "add");
        opt.put(10, "sub");
        opt.put(12, "mul");
        opt.put(15, "sdiv");
        opt.put(18, "srem");
        registerInfo = new ArrayList<String>();
        for (int i = 3; i <= 6; i++) registerInfo.add("t" + i);  // 0 - 3
        for (int i = 1; i <= 7; i++) registerInfo.add("a" + i);  // 4 - 10
        for (int i = 1; i <= 11; i++) registerInfo.add("s" + i); // 11 - 21
    }

    public AsmBuilder() {
        buffer = new StringBuffer();
        offsetMp = new HashMap<String, Integer>();
        variableMp = new HashMap<String, Integer>();
        globalVar = new HashSet<String>();
        registerId = new HashMap<String, Integer>();
        t1 = t2 = t0 = "";
    }

    public void get(LLVMValueRef var, String dest) {  
        // var LLVMValue
        // dest : register name
        String name = LLVMGetValueName(var).getString();

        if (!name.equals("") && (name.equals(t0) || name.equals(t1) || name.equals(t2))) {
            if (name.equals(t0)) { op1("mv", dest, "t0"); }
            if (name.equals(t1)) { op1("mv", dest, "t1"); }
            if (name.equals(t2)) { op1("mv", dest, "t2"); }
            return ;
        }
        if (name.equals("")) { // const int
            op1("li", dest, "" + LLVMConstIntGetSExtValue(var));
            if (dest.equals("t0")) { t0 = ""; }
            if (dest.equals("t1")) { t1 = ""; }
            if (dest.equals("t2")) { t2 = ""; }
        } else if (globalVar.contains(name)) {  // global var isn't on stack
                op1("la", dest, name);
                buffer.append(String.format("  lw %s, 0(%s)\n", dest, dest));
                if (dest.equals("t0")) { t0 = name; }
                if (dest.equals("t1")) { t1 = name; }
                if (dest.equals("t2")) { t2 = name; }
        } else {
            int id = registerId.get(name);
            if (id == 22) {
                if (t0.equals(name)) {
                    op1("mv", dest, "t0");
                } if (t1.equals(name)) {
                    // buffer.append("t1 ==== !! " + name + "\n");
                    op1("mv", dest, "t1");
                } if (t2.equals(name)) {
                    op1("mv", dest, "t2");
                } else {
                    buffer.append(String.format("  lw %s, %d(sp)\n", dest, offsetMp.get(name)));
                    if (dest.equals("t0")) { t0 = name; }
                    if (dest.equals("t1")) { t1 = name; }
                    if (dest.equals("t2")) { t2 = name; }
                }
            } else {
                String registerName = registerInfo.get(id);
                op1("mv", dest, registerName);
                if (dest.equals("t0")) { t0 = registerName; }
                if (dest.equals("t1")) { t1 = registerName; }
                if (dest.equals("t2")) { t2 = registerName; }
            }
        }
    }
    public void push(String source, String dest) {
        // source: register name
        // dest: var name
        int id = registerId.get(dest);
        if (id == 22) {
            buffer.append(String.format("  sw %s, %d(sp)\n", source, offsetMp.get(dest)));
        } else {
            String registerName = registerInfo.get(id);
            op1("mv", registerName, source);
        }
    }

    public void output(int opType, int opNum, LLVMValueRef inst, LLVMValueRef op1, LLVMValueRef op2) {
        if (opType == 1) { // ret -> call exit
            String name = LLVMGetValueName(op1).getString();
            get(op1, "a0");
        } else if (opType == 8 || opType == 10 || opType == 12 || opType == 15 || opType == 18) {
            String nameDest = LLVMGetValueName(inst).getString();
            String name1 = LLVMGetValueName(op1).getString();
            String name2 = LLVMGetValueName(op2).getString();

            if (opType == 8 && (name1.equals("") || name2.equals(""))) {
                if (name1.equals("")) {  // 1 + a
                    get(op2, "t2");
                    op2(opt.get(opType) + "i", "t0", "t2", "" + LLVMConstIntGetSExtValue(op1));
                    t2 = name2;
                } else {                 // a + 1
                    get(op1, "t1");
                    op2(opt.get(opType) + "i", "t0", "t1", "" + LLVMConstIntGetSExtValue(op2));
                    t1 = name1;
                }
            } else {
                get(op1, "t1");
                get(op2, "t2");
                op2(opt.get(opType), "t0", "t1", "t2");
                t1 = name1;
                t2 = name2;
            }
            
            push("t0", nameDest);
            t0 = nameDest;
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
        String dest = LLVMGetValueName(op2).getString();
        String source = LLVMGetValueName(op1).getString();

        if (globalVar.contains(source)) {
            op1("la", "t0", source);
            buffer.append(String.format("  lw t0, 0(t0)\n"));
            t0 = source;
            // buffer.append("t0 == " + source);
        } else {
            get(op1, "t0");
        }


        push("t0", dest);
    }
    public void store(LLVMValueRef op1, LLVMValueRef op2, String lhs) {
        String dest = LLVMGetValueName(op2).getString();
        get(op1, "t0");
        if (globalVar.contains(dest)) {
            op1("la", "t1", dest);
            t1 = "";
            buffer.append(String.format("  sw t0, 0(t1)\n"));
        } else {
            push("t0", dest);
        }
        t0 = "";
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
