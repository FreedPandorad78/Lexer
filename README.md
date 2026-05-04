# Lexer de Java

Analizador léxico (lexer) para un subconjunto del lenguaje Java, desarrollado como proyecto de compiladores.

## ¿Qué hace?

Lee código fuente Java y lo descompone en **tokens**: palabras clave, identificadores, operadores, literales, delimitadores y comentarios. Reporta el tipo, valor, línea y columna de cada token encontrado.

## Compilar y ejecutar

```bash
# Compilar
javac *.java

# Ejecutar (REPL interactivo)
java Main
```

## Uso del REPL

Al ejecutar el programa aparece un prompt donde puedes escribir código Java directamente:

```
java-lexer> int x = 42;
```

Salida:
```
-----------------------------------------------------------------
#    TIPO                      VALOR                LÍNEA  COL
-----------------------------------------------------------------
1    INT                       "int"                1      1
2    IDENTIFIER                "x"                  1      5
3    ASSIGN                    "="                  1      7
4    INTEGER_LITERAL           "42"                 1      9
5    SEMICOLON                 ";"                  1      11
-----------------------------------------------------------------
Total de tokens: 5
```

### Comandos disponibles

| Comando | Descripción |
|---------|-------------|
| `exit` | Salir del programa |
| `file <ruta>` | Analizar un archivo `.java` existente |

**Ejemplo con archivo:**
```
java-lexer> file src/MiArchivo.java
```

## Tokens soportados

| Categoría | Ejemplos |
|-----------|---------|
| Literales | `42`, `3.14f`, `"hola"`, `'a'`, `true`, `null` |
| Palabras clave | `int`, `if`, `while`, `class`, `return`, ... |
| Operadores aritméticos | `+` `-` `*` `/` `%` |
| Asignación | `=` `+=` `-=` `*=` `/=` `%=` |
| Comparación | `==` `!=` `<` `<=` `>` `>=` |
| Lógicos | `&&` `\|\|` `!` |
| Incremento/decremento | `++` `--` |
| Bitwise | `&` `\|` `^` `~` `<<` `>>` |
| Delimitadores | `(` `)` `{` `}` `[` `]` `;` `,` `.` `:` `?` |
| Comentarios | `// línea` y `/* bloque */` |

## Estructura del proyecto

```
Lexer/
├── TokenType.java   # Enum con todos los tipos de token
├── Token.java       # Clase que representa un token individual
├── Lexer.java       # Implementación del analizador léxico
└── Main.java        # REPL interactivo (punto de entrada)
```

## Limitaciones conocidas

- No soporta literales hexadecimales (`0xFF`) ni octales (`077`)
- No soporta anotaciones (`@Override`)
- Un comentario de bloque sin cerrar (`/* ...`) no genera error explícito
- No soporta el operador `>>>` (unsigned right shift)
- No es un parser: valida tokens, no la sintaxis completa del programa
