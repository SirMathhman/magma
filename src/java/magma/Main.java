package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("Main.c");
            Files.writeString(target, Results.unwrap(compile(input)));
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Result<String, CompileException> compile(String root) throws CompileException {
        final var segments = splitStatements(root);
        Result<StringBuilder, CompileException> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output.and(() -> compileRootSegment(segment.strip())).mapValue(tuple -> tuple.left().append(tuple.right()));
        }
        return output.mapValue(StringBuilder::toString);
    }

    private static List<String> splitStatements(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
            if (c == '{') depth++;
            if (c == '}') depth--;
        }
        advance(buffer, segments);
        return segments;
    }

    private static Result<String, CompileException> compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return new Ok<>("");
        if (rootSegment.startsWith("import ")) return new Ok<>("#include \"temp.h\"\n");

        return splitAtSlice(rootSegment, "class ").<Result<String, CompileException>>flatMap(withoutClass -> splitAtSlice(withoutClass.right(), "{").flatMap(withoutContentStart -> {
            final var name = withoutContentStart.left().strip();
            final var withEnd = withoutContentStart.right();
            if (!withEnd.endsWith("}")) return Optional.empty();
            return Optional.of(new Ok<>("struct " + name + " {\n}"));
        })).orElseGet(() -> new Err<>(new CompileException("Unknown root segment", rootSegment)));
    }

    private static Optional<Tuple<String, String>> splitAtSlice(String input, String slice) {
        final var index = input.indexOf(slice);
        if (index == -1) return Optional.empty();

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());
        return Optional.of(new Tuple<>(left, right));
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
