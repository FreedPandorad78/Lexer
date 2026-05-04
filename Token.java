// Representa un token individual producido por el lexer.
// Cada token tiene un tipo, el texto original, y su posición en el fuente.
public class Token {
    public final TokenType type;
    public final String value;  // texto exacto del token en el fuente
    public final int line;      // línea donde aparece (empieza en 1)
    public final int column;    // columna donde empieza (empieza en 1)

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return String.format("Token(%-25s %-20s line=%-4d col=%d)",
                type.name() + ",", "\"" + value + "\",", line, column);
    }
}