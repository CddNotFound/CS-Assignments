public class ArrayType extends Type {
    Type elementType;
    int num;    

    public ArrayType() {
        this.elementType = null;
        int num = 0;
    }

    public ArrayType(Type elementType, int num) {
        this.elementType = elementType;
        this.num = num;
    }
}
