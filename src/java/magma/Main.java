package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            final var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(segments, buffer);
        return segments;
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return "#include \"temp.h\"\n";

        return compileClass(rootSegment).orElseThrow(() -> new CompileException("Unknown root segment", rootSegment));
    }

    private static Optional<String> compileClass(String rootSegment) {
        return split(rootSegment, "class ", tuple -> {
            return split(tuple.right(), "{", tuple0 -> {
                return Optional.of("struct " + tuple0.left().strip() + " {\n}");
            });
        });
    }

    private static Optional<String> split(String rootSegment, String infix, Function<Tuple<String, String>, Optional<? extends String>> mapper) {
        final var index = rootSegment.indexOf(infix);
        if (index == -1) return Optional.empty();

        final var left = rootSegment.substring(0, index);
        final var right = rootSegment.substring(index + infix.length());
        return Optional.of(new Tuple<>(left, right)).flatMap(mapper);
    }

    private static void advance(List<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
