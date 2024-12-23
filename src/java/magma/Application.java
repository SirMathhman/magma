package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Application {
    public static final Path SOURCE = Paths.get(".", "temp.java");
    public static final Path TARGET = Paths.get(".", "temp.c");
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATEMENT_TERMINATOR = ";";
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";

    static void run(Path source) throws IOException, CompileException {
        if (!Files.exists(source)) return;

        final var input = Files.readString(source);
        final var output = input.isEmpty() ? "" : compileRoot(input);

        final var name = source.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExtension = name.substring(0, separator);
        Files.writeString(source.resolveSibling(nameWithoutExtension + ".c"), output);
    }

    static String compileRoot(String root) throws CompileException {
        final var segments = split(root);
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileRootStatement(segment));
        }

        return buffer.toString();
    }

    private static List<String> split(String root) {
        var state = new State();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }
        return state.advance().segments();
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';') return appended.advance();
        return appended;
    }

    private static String compileRootStatement(String rootStatement) throws CompileException {
        if (rootStatement.startsWith(PACKAGE_KEYWORD_WITH_SPACE) && rootStatement.endsWith(STATEMENT_TERMINATOR))
            return "";

        if (rootStatement.startsWith(IMPORT_KEYWORD_WITH_SPACE) && rootStatement.endsWith(STATEMENT_TERMINATOR))
            return rootStatement;

        throw new CompileException("Unknown root statement", rootStatement);
    }
}