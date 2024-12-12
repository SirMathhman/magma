package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "working", "main.mgs");
            final var input = Files.readString(source);
            Files.writeString(source.resolveSibling("main.casm"), compile(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compile(String input) {
        final var segments = split(input);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment));
        }

        return output.toString();
    }

    private static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static String compileRootSegment(String rootSegment) {
        final var stripped = rootSegment.strip();
        return compileFunction(stripped)
                .or(() -> compileAlias(stripped))
                .orElse(rootSegment);
    }

    private static Optional<String> compileAlias(String rootSegment) {
        if (rootSegment.startsWith("type ")) return Optional.of("");
        else return Optional.empty();
    }

    private static Optional<String> compileFunction(String input) {
        if (!input.startsWith("def ")) return Optional.empty();
        final var afterKeyword = input.substring("def ".length());

        final var paramStart = afterKeyword.indexOf('(');
        if (paramStart == -1) return Optional.empty();

        final var name = afterKeyword.substring(0, paramStart).strip();
        final var blockStart = afterKeyword.indexOf('{');
        if (blockStart == -1) return Optional.empty();

        final var withEnd = afterKeyword.substring(blockStart + 1).strip();
        if (!withEnd.endsWith("}")) return Optional.empty();
        final var content = withEnd.substring(0, withEnd.length() - "}".length());
        final var split = split(content);

        var output = new StringBuilder();
        for (String statement : split) {
            if (!statement.isBlank()) {
                output.append(compileStatement(statement));
            }
        }

        return Optional.of("label " + name + " = {\n" +
                output +
                "}");
    }

    private static String compileStatement(String statement) {
        return "\t" + statement + "\n";
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
