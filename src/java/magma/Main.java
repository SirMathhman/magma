package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("Main.mgs");
            Files.writeString(target, compile(input));
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String input) throws CompileException {
        final var segments = split(input);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment));
        }

        return output.toString();
    }

    private static List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                append(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        append(buffer, segments);
        return segments;
    }

    private static String compileRootSegment(String segment) throws CompileException {
        final var stripped = segment.strip();
        if (stripped.startsWith("package ")) return "";
        if (stripped.startsWith("import ")) return stripped;


        throw new CompileException("Unknown root segment", stripped);
    }

    private static void append(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
