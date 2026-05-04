import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Analizador léxico (lexer) para un subconjunto de Java.
// Lee el código fuente carácter por carácter y produce una lista de tokens.
// Los errores léxicos se acumulan en la lista 'errors' en lugar de lanzar
// una excepción, para poder reportar todos los problemas de una sola vez.
public class Lexer {

    // Mapa de palabras reservadas de Java -> tipo de token correspondiente
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("abstract",   TokenType.ABSTRACT);
        KEYWORDS.put("boolean",    TokenType.BOOLEAN);
        KEYWORDS.put("break",      TokenType.BREAK);
        KEYWORDS.put("byte",       TokenType.BYTE);
        KEYWORDS.put("case",       TokenType.CASE);
        KEYWORDS.put("catch",      TokenType.CATCH);
        KEYWORDS.put("char",       TokenType.CHAR);
        KEYWORDS.put("class",      TokenType.CLASS);
        KEYWORDS.put("continue",   TokenType.CONTINUE);
        KEYWORDS.put("default",    TokenType.DEFAULT);
        KEYWORDS.put("do",         TokenType.DO);
        KEYWORDS.put("double",     TokenType.DOUBLE);
        KEYWORDS.put("else",       TokenType.ELSE);
        KEYWORDS.put("extends",    TokenType.EXTENDS);
        KEYWORDS.put("final",      TokenType.FINAL);
        KEYWORDS.put("finally",    TokenType.FINALLY);
        KEYWORDS.put("float",      TokenType.FLOAT);
        KEYWORDS.put("for",        TokenType.FOR);
        KEYWORDS.put("if",         TokenType.IF);
        KEYWORDS.put("implements", TokenType.IMPLEMENTS);
        KEYWORDS.put("import",     TokenType.IMPORT);
        KEYWORDS.put("instanceof", TokenType.INSTANCEOF);
        KEYWORDS.put("int",        TokenType.INT);
        KEYWORDS.put("interface",  TokenType.INTERFACE);
        KEYWORDS.put("long",       TokenType.LONG);
        KEYWORDS.put("new",        TokenType.NEW);
        KEYWORDS.put("package",    TokenType.PACKAGE);
        KEYWORDS.put("private",    TokenType.PRIVATE);
        KEYWORDS.put("protected",  TokenType.PROTECTED);
        KEYWORDS.put("public",     TokenType.PUBLIC);
        KEYWORDS.put("return",     TokenType.RETURN);
        KEYWORDS.put("short",      TokenType.SHORT);
        KEYWORDS.put("static",     TokenType.STATIC);
        KEYWORDS.put("super",      TokenType.SUPER);
        KEYWORDS.put("switch",     TokenType.SWITCH);
        KEYWORDS.put("this",       TokenType.THIS);
        KEYWORDS.put("throw",      TokenType.THROW);
        KEYWORDS.put("throws",     TokenType.THROWS);
        KEYWORDS.put("try",        TokenType.TRY);
        KEYWORDS.put("void",       TokenType.VOID);
        KEYWORDS.put("while",      TokenType.WHILE);
        // true, false y null son literales, no palabras clave,
        // pero en Java se tratan como reservadas
        KEYWORDS.put("true",       TokenType.BOOLEAN_LITERAL);
        KEYWORDS.put("false",      TokenType.BOOLEAN_LITERAL);
        KEYWORDS.put("null",       TokenType.NULL_LITERAL);
    }

    private final String source;
    private int pos = 0;
    private int line = 1;
    private int column = 1;

    // Lista de errores léxicos encontrados durante el análisis.
    // Se usa una lista para no detener el análisis al primer error.
    public final List<String> errors = new ArrayList<>();

    public Lexer(String source) {
        this.source = source;
    }

    // Retorna el carácter actual sin avanzar. Retorna '\0' si llegó al final.
    private char current() {
        return pos < source.length() ? source.charAt(pos) : '\0';
    }

    // Permite "espiar" un carácter adelante sin consumirlo.
    // Útil para reconocer operadores de dos caracteres como ==, !=, <=, etc.
    private char peek(int offset) {
        int idx = pos + offset;
        return idx < source.length() ? source.charAt(idx) : '\0';
    }

    // Consume el carácter actual y actualiza línea/columna.
    private char advance() {
        char ch = source.charAt(pos++);
        if (ch == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        return ch;
    }

    private Token makeToken(TokenType type, String value, int line, int col) {
        return new Token(type, value, line, col);
    }

    private void skipWhitespace() {
        while (pos < source.length() && Character.isWhitespace(current())) {
            advance();
        }
    }

    private Token readLineComment(int line, int col) {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && current() != '\n') {
            sb.append(advance());
        }
        return makeToken(TokenType.LINE_COMMENT, sb.toString().trim(), line, col);
    }

    // TODO: si el bloque no se cierra (falta "*/"), no se genera error.
    //       Sería bueno detectarlo en el futuro.
    private Token readBlockComment(int line, int col) {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length()) {
            if (current() == '*' && peek(1) == '/') {
                advance(); advance(); // consumir */
                break;
            }
            sb.append(advance());
        }
        return makeToken(TokenType.BLOCK_COMMENT, sb.toString().trim(), line, col);
    }

    private Token readString(int line, int col) {
        advance(); // consumir la comilla de apertura "
        StringBuilder sb = new StringBuilder();
        while (pos < source.length()) {
            char ch = current();
            if (ch == '"') {
                advance(); // consumir la comilla de cierre
                break;
            }
            if (ch == '\n') {
                errors.add("[Línea " + line + ", Col " + col + "] String no cerrado");
                break;
            }
            if (ch == '\\') {
                // secuencia de escape
                advance();
                char esc = advance();
                switch (esc) {
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case 'r'  -> sb.append('\r');
                    case '"'  -> sb.append('"');
                    case '\'' -> sb.append('\'');
                    case '\\' -> sb.append('\\');
                    default   -> sb.append('\\').append(esc); // escape no reconocido, se deja tal cual
                }
            } else {
                sb.append(advance());
            }
        }
        return makeToken(TokenType.STRING_LITERAL, sb.toString(), line, col);
    }

    private Token readChar(int line, int col) {
        advance(); // consumir la comilla simple de apertura '
        StringBuilder sb = new StringBuilder();
        if (current() == '\\') {
            advance();
            char esc = advance();
            switch (esc) {
                case 'n'  -> sb.append('\n');
                case 't'  -> sb.append('\t');
                case '\'' -> sb.append('\'');
                case '\\' -> sb.append('\\');
                default   -> sb.append(esc);
            }
        } else if (current() != '\'') {
            sb.append(advance());
        }
        // verificar que se cierre con '
        if (current() == '\'') {
            advance();
        } else {
            errors.add("[Línea " + line + ", Col " + col + "] Char literal no cerrado");
        }
        return makeToken(TokenType.CHAR_LITERAL, sb.toString(), line, col);
    }

    // Lee un literal numérico entero o flotante.
    // Soporta sufijos: L/l (long), F/f (float), D/d (double).
    // TODO: no soporta hexadecimales (0xFF) ni octales (077).
    private Token readNumber(int line, int col) {
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;
        while (pos < source.length() && (Character.isDigit(current()) || current() == '.')) {
            if (current() == '.') {
                if (isFloat) break; // segundo punto: no es parte del número
                isFloat = true;
            }
            sb.append(advance());
        }
        // sufijos opcionales: L/l para long, F/f para float, D/d para double
        if ("LlFfDd".indexOf(current()) >= 0) {
            char suffix = advance();
            sb.append(suffix);
            if ("FfDd".indexOf(suffix) >= 0) isFloat = true;
        }
        return makeToken(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INTEGER_LITERAL, sb.toString(), line, col);
    }

    // Lee un identificador o palabra clave.
    // Primero acumula todos los caracteres válidos, luego busca en el mapa
    // de palabras clave. Si no está, es un identificador de usuario.
    private Token readIdentifierOrKeyword(int line, int col) {
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && (Character.isLetterOrDigit(current()) || current() == '_')) {
            sb.append(advance());
        }
        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
        return makeToken(type, word, line, col);
    }

    // Produce el siguiente token del fuente.
    // Es el método principal del lexer.
    public Token nextToken() {
        skipWhitespace();
        if (pos >= source.length()) return makeToken(TokenType.EOF, "", line, column);

        int l = line, c = column;
        char ch = current();

        // Comentarios y slash: hay que mirar el siguiente carácter para distinguir
        if (ch == '/') {
            if (peek(1) == '/') { advance(); advance(); return readLineComment(l, c); }
            if (peek(1) == '*') { advance(); advance(); return readBlockComment(l, c); }
            if (peek(1) == '=') { advance(); advance(); return makeToken(TokenType.SLASH_ASSIGN, "/=", l, c); }
            advance();
            return makeToken(TokenType.SLASH, "/", l, c);
        }

        if (ch == '"') return readString(l, c);
        if (ch == '\'') return readChar(l, c);
        if (Character.isDigit(ch)) return readNumber(l, c);
        if (Character.isLetter(ch) || ch == '_') return readIdentifierOrKeyword(l, c);

        // Operadores de dos caracteres: usamos peek(1) para ver el siguiente
        // sin consumirlo, y solo avanzamos si coincide el par completo.
        String two = "" + ch + peek(1);
        switch (two) {
            case "==" -> { advance(); advance(); return makeToken(TokenType.EQUAL,          "==", l, c); }
            case "!=" -> { advance(); advance(); return makeToken(TokenType.NOT_EQUAL,      "!=", l, c); }
            case "<=" -> { advance(); advance(); return makeToken(TokenType.LESS_EQUAL,     "<=", l, c); }
            case ">=" -> { advance(); advance(); return makeToken(TokenType.GREATER_EQUAL,  ">=", l, c); }
            case "&&" -> { advance(); advance(); return makeToken(TokenType.AND,            "&&", l, c); }
            case "||" -> { advance(); advance(); return makeToken(TokenType.OR,             "||", l, c); }
            case "++" -> { advance(); advance(); return makeToken(TokenType.INCREMENT,      "++", l, c); }
            case "--" -> { advance(); advance(); return makeToken(TokenType.DECREMENT,      "--", l, c); }
            case "+=" -> { advance(); advance(); return makeToken(TokenType.PLUS_ASSIGN,    "+=", l, c); }
            case "-=" -> { advance(); advance(); return makeToken(TokenType.MINUS_ASSIGN,   "-=", l, c); }
            case "*=" -> { advance(); advance(); return makeToken(TokenType.STAR_ASSIGN,    "*=", l, c); }
            case "%=" -> { advance(); advance(); return makeToken(TokenType.PERCENT_ASSIGN, "%=", l, c); }
            case "<<" -> { advance(); advance(); return makeToken(TokenType.LEFT_SHIFT,     "<<", l, c); }
            case ">>" -> { advance(); advance(); return makeToken(TokenType.RIGHT_SHIFT,    ">>", l, c); }
        }

        // Operadores y delimitadores de un solo carácter
        advance();
        return switch (ch) {
            case '+' -> makeToken(TokenType.PLUS,      "+", l, c);
            case '-' -> makeToken(TokenType.MINUS,     "-", l, c);
            case '*' -> makeToken(TokenType.STAR,      "*", l, c);
            case '%' -> makeToken(TokenType.PERCENT,   "%", l, c);
            case '=' -> makeToken(TokenType.ASSIGN,    "=", l, c);
            case '<' -> makeToken(TokenType.LESS,      "<", l, c);
            case '>' -> makeToken(TokenType.GREATER,   ">", l, c);
            case '!' -> makeToken(TokenType.NOT,       "!", l, c);
            case '&' -> makeToken(TokenType.AMPERSAND, "&", l, c);
            case '|' -> makeToken(TokenType.PIPE,      "|", l, c);
            case '^' -> makeToken(TokenType.CARET,     "^", l, c);
            case '~' -> makeToken(TokenType.TILDE,     "~", l, c);
            case '(' -> makeToken(TokenType.LPAREN,    "(", l, c);
            case ')' -> makeToken(TokenType.RPAREN,    ")", l, c);
            case '{' -> makeToken(TokenType.LBRACE,    "{", l, c);
            case '}' -> makeToken(TokenType.RBRACE,    "}", l, c);
            case '[' -> makeToken(TokenType.LBRACKET,  "[", l, c);
            case ']' -> makeToken(TokenType.RBRACKET,  "]", l, c);
            case ';' -> makeToken(TokenType.SEMICOLON, ";", l, c);
            case ',' -> makeToken(TokenType.COMMA,     ",", l, c);
            case '.' -> makeToken(TokenType.DOT,       ".", l, c);
            case ':' -> makeToken(TokenType.COLON,     ":", l, c);
            case '?' -> makeToken(TokenType.QUESTION,  "?", l, c);
            default  -> {
                errors.add("[Línea " + l + ", Col " + c + "] Carácter desconocido: '" + ch + "'");
                yield makeToken(TokenType.UNKNOWN, String.valueOf(ch), l, c);
            }
        };
    }

    // Tokeniza todo el fuente y retorna la lista completa de tokens.
    // Siempre termina con un token EOF.
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        Token tok;
        do {
            tok = nextToken();
            tokens.add(tok);
        } while (tok.type != TokenType.EOF);
        return tokens;
    }
}