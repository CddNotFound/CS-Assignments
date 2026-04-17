import java.util.HashMap;

import org.bytedeco.llvm.LLVM.LLVMValueRef;

public class Scope {
    private String scopeName;
    private Scope parent;
    HashMap<String, LLVMValueRef> mp;
    private static int scopeCnt;

    static {
        scopeCnt = 0;
    }

    public Scope(String scopeName, Scope currentScope) {
        scopeCnt += 1;
        this.scopeName = scopeName + "#" + scopeCnt;
        parent = currentScope;
        mp = new HashMap<String, LLVMValueRef>();
    }

    public String getScopeName() {
        return scopeName;
    }

    public Scope getEnclosScope() {
        return parent;
    }

    public void define(String name, LLVMValueRef info) {
        // System.out.println("define in " + scopeName + ": " + name);
        mp.put(name, info);
    }

    public LLVMValueRef resolve(String name) {
        Scope tmpScope = this;
        while (tmpScope != null) {
            // System.out.println("Search '" + name + "' in " + tmpScope.scopeName);
            LLVMValueRef type = tmpScope.mp.getOrDefault(name, null);
            if (type != null) {
                // System.out.println("found!" + type.getClass());
                return type;
            }
            tmpScope = tmpScope.parent;
        }
        return null;
    }

    public LLVMValueRef countCurrentScope(String name) {
        return mp.getOrDefault(name, null);
    }

    // public void exitScope() {
    //     // delete 
    //     currentScope = parent;
    // }

}
