package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("Main.c");
            final var output = splitAndCompile(input, Main::compileRootSegment);
            Files.writeString(target, Results.unwrap(output));
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Result<String, CompileException> splitAndCompile(String root, Function<String, Result<String, CompileException>> compiler) {
        return splitStatements(root).flatMapValue(segments -> compileAndJoin(compiler, segments));
    }

    private static Result<String, CompileException> compileAndJoin(Function<String, Result<String, CompileException>> compiler, List<String> segments) {
        Result<StringBuilder, CompileException> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output.and(() -> compiler.apply(segment.strip())).mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static Result<List<String>, CompileException> splitStatements(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        var queue = IntStream.range(0, input.length())
                .mapToObj(index -> input.charAt(index))
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                final var maybeEscaped = queue.pop();
                buffer.append(maybeEscaped);
                if (maybeEscaped == '\\') {
                    buffer.append(queue.pop());
                }

                buffer.append(queue.pop());
            }

            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }

            if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            }

            if (c == '{') depth++;
            if (c == '}') depth--;
        }
        advance(buffer, segments);
        if (depth == 0) {
            return new Ok<>(segments);
        } else {
            return new Err<>(new CompileException("Invalid depth '" + depth + "'", input));
        }
    }

    private static Result<String, CompileException> compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return new Ok<>("");
        if (rootSegment.startsWith("import ")) return new Ok<>("#include \"temp.h\"\n");

        return splitAtSlice(rootSegment, "class ").flatMap(withoutClass -> splitAtSlice(withoutClass.right(), "{").flatMap(withoutContentStart -> {
            final var name = withoutContentStart.left().strip();
            final var withEnd = withoutContentStart.right();
            if (!withEnd.endsWith("}")) return Optional.empty();
            final var content = withEnd.substring(0, withEnd.length() - "}".length());
            return Optional.of(splitAndCompile(content, Main::compileStructSegment)
                    .mapValue(compiled -> "struct " + name + " {" + compiled + "\n}"));

        })).orElseGet(() -> new Err<>(new CompileException("Unknown root segment", rootSegment)));
    }

    private static Result<String, CompileException> compileStructSegment(String structSegment) {
        if (structSegment.contains("(")) return new Ok<>("\n\tvoid temp(){\n\t}");
        return new Err<>(new CompileException("Invalid struct segment", structSegment));
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
