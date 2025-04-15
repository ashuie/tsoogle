package assignment;

public class Token {
    public enum Type { AND, OR, NOT, LEFTPAREN, RIGHTPAREN, WORD };

    Type type;
    String val;

    public Token(Type type) {
        this.type = type;
    }

    public Token(String val) {
        this.type = Type.WORD;
        this.val = val;
    }

    public String toString() {
        if (type == Type.WORD) {
            return "WORD(" + val + ")";
        }
        return type.name();
    }
}