import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    static final String BANNER = """
            ╔══════════════════════════════════════════╗
            ║        Java Lexer - REPL v1.0           ║
            ║  Escribe código Java y ve sus tokens    ║
            ║  Comandos:                              ║
            ║    exit          → salir                ║
            ║    file <ruta>   → leer archivo         ║
            ╚══════════════════════════════════════════╝
            """;

    public static void main(String[] args) {
        System.out.println(BANNER);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("java-lexer> ");
            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("¡Hasta luego!");
                break;
            }

            if (input.toLowerCase().startsWith("file ")) {
                String path = input.substring(5).trim();
                try {
                    String source = Files.readString(Paths.get(path));
                    System.out.println("\nArchivo cargado: " + path);
                    runLexer(source);
                } catch (IOException e) {
                    System.out.println("Error: no se encontró el archivo '" + path + "'");
                }
                continue;
            }

            // Soporte multilínea: si termina en { pedimos más input
            StringBuilder sb = new StringBuilder(input);
            if (input.endsWith("{") || (!input.endsWith(";") && !input.endsWith("}"))) {
                System.out.println("  (Presiona Enter en línea vacía para terminar)");
                while (scanner.hasNextLine()) {
                    System.out.print("... ");
                    String extra = scanner.nextLine();
                    if (extra.isEmpty()) break;
                    sb.append("\n").append(extra);
                }
            }

            runLexer(sb.toString());
        }

        scanner.close();
    }

    static void runLexer(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.tokenize();
        printTokens(tokens);
        if (!lexer.errors.isEmpty()) {
            System.out.println("Errores léxicos:");
            for (String err : lexer.errors) {
                System.out.println("   " + err);
            }
            System.out.println();
        }
    }

    static void printTokens(List<Token> tokens) {
        String sep = "─".repeat(65);
        System.out.println("\n" + sep);
        System.out.printf("%-4s %-25s %-20s %-6s %s%n", "#", "TIPO", "VALOR", "LÍNEA", "COL");
        System.out.println(sep);
        int count = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token tok = tokens.get(i);
            if (tok.type == TokenType.EOF) break;
            System.out.printf("%-4d %-25s %-20s %-6d %d%n",
                    i + 1,
                    tok.type.name(),
                    "\"" + tok.value + "\"",
                    tok.line,
                    tok.column);
            count++;
        }
        System.out.println(sep);
        System.out.println("Total de tokens: " + count + "\n");
    }
}
