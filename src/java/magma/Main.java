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
            final var target = source.resolveSibling("Main.c");
            final var output = compile(input);
            Files.writeString(target, output);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String root) throws CompileException {
        final var segments = split(root);
        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment.strip()));
        }
        return output.toString();
    }

    private static ArrayList<String> split(String root) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            final var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return "#include \"temp.h\"";
        throw new CompileException("Unknown root segment", rootSegment);
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
