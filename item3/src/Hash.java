public class Hash {
    int base;
    int offset;
    int size;
    int literal;//변수에 저장된 값
    boolean literalBool = false;//변수에 값이 저장되있으면 true 저장되지 않았으면 false
    public Hash(int a, int b, int c){
        this.base = a;
        this.offset = b;
        this.size = c;
    }

    public Hash(int a, int b, int c, int d, boolean Bool){
        this.base = a;
        this.offset = b;
        this.size = c;
        this.literal = d;
        this.literalBool = Bool;
    }

}
