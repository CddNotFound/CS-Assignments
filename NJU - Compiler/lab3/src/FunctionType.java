import java.util.ArrayList;

public class FunctionType extends Type {
    Type retType;
    ArrayList<Type> parametersType;

    public FunctionType() {
        this.retType = null;
        this.parametersType = new ArrayList<Type>();
    }

    public void addParameter(Type paraType) {
        parametersType.add(paraType);
    }
}
