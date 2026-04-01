import java.util.HashMap;

public class Scope {
    private String scopeName;
    private Scope parent;
    HashMap<String, Type> mp;

    public Scope(String scopeName, Scope currentScope) {
        this.scopeName = scopeName;
        parent = currentScope;
        mp = new HashMap<String, Type>();
    }

    public String getScopeName() {
        return scopeName;
    }

    public Scope getEnclosScope() {
        return parent;
    }
    
    public void define(String name, Type info) {
        mp.put(name, info);
    }

    public Type resolve(String name) {
        Scope tmpScope = this;
        while (tmpScope != null) {
            Type type = mp.getOrDefault(name, null);
            if (type != null) {
                return type;
            }
            tmpScope = tmpScope.parent;
        }
        return null;
    }

    public Type countCurrentScope(String name) {
        return mp.getOrDefault(name, null);
    }

    // public void exitScope() {
    //     // delete 
    //     currentScope = parent;
    // }

}
