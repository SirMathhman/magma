package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var output = compileRoot(input);
            final var target = source.resolveSibling("Temp.java0");
            Files.writeString(target, output);
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compileRoot(String root) throws CompileException {
        final var segments = split(root);
        final var buffer = new StringBuilder();
        for (String segment : segments) {
            final var leadingTuple = trimLeading(segment);
            final var trailingTuple = trimTrailing(leadingTuple.right());

            final var stripped = trailingTuple.right();
            final var leading = leadingTuple.left();
            final var trailing = trailingTuple.left();
            final var compiled = compileRootSegment(stripped);
            buffer.append(leading).append(compiled).append(trailing);
        }
        return buffer.toString();
    }

    private static Tuple<String, String> trimTrailing(String segment) {
        for (int i = segment.length() - 1; i >= 0; i--) {
            var c = segment.charAt(i);
            if (!Character.isWhitespace(c)) {
                final var withoutTrailing = segment.substring(0, i + 1);
                final var trailing = i == segment.length() - 1 ? "" : segment.substring(i + 1);
                return new Tuple<>(trailing, withoutTrailing);
            }
        }

        return new Tuple<>(segment, "");
    }

    private static Tuple<String, String> trimLeading(String segment) {
        for (int i = 0; i < segment.length(); i++) {
            var c = segment.charAt(i);
            if (!Character.isWhitespace(c)) {
                final var leading = segment.substring(0, i);
                final var withoutLeading = segment.substring(i);
                return new Tuple<>(leading, withoutLeading);
            }
        }

        return new Tuple<>(segment, "");
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
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

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "package temp;";
        if (rootSegment.startsWith("import ")) return "import temp;";
        if (rootSegment.contains("class ")) return "class Temp {}";
        throw new CompileException("Unknown root segment", rootSegment);
    }
}
