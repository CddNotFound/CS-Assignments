import java.util.ArrayList;
import java.util.HashMap;

import org.bytedeco.llvm.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.*;

public class AsmBuilder {
    StringBuffer buffer;

    public AsmBuilder() {
        buffer = new StringBuffer();
    }

    public void output(int opType, int opNum, LLVMValueRef op1, LLVMValueRef op2) {
        // System.out.println("opType" + "= " + opType);
        if (opType == 1) {
            op1("li", "a0", "" + LLVMConstIntGetSExtValue(op1));
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
