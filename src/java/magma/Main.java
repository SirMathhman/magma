package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("Main.c");
            final var output = Results.unwrap(compile(input));
            Files.writeString(target, output);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Result<String, CompileException> compile(String root) {
        final var segments = split(root);
        Result<StringBuilder, CompileException> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output.and(() -> compileRootSegment(segment.strip())).mapValue(inner -> {
                return inner.left().append(inner.right());
            });
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static List<String> split(String root) {
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

    private static Result<String, CompileException> compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return new Ok<>("");
        if (rootSegment.startsWith("import ")) return new Ok<>("#include \"temp.h\"\n");

        final var result = compileClass(rootSegment);
        if (result.isValid()) return result;
        return new Err<>(new CompileException("Unknown root segment", rootSegment));
    }

    private static Result<String, CompileException> compileClass(String rootSegment) {
        return split(rootSegment, "class ", modifiers -> new Ok<>(""), afterKeyword -> {
            return split(afterKeyword, "{", name -> {
                return new Ok<>(name.strip());
            }, withEnd -> {
                return truncateRight(withEnd.strip(), "}", content -> {
                    return new Ok<>("");
                });
            }, (left, _) -> left + " {}");
        }, (left, right) -> left + "struct " + right);
    }

    private static Result<String, CompileException> truncateRight(String input, String slice, Function<String, Result<String, CompileException>> mapper) {
        if (input.endsWith(slice)) {
            return mapper.apply(input.substring(0, input.length() - slice.length()));
        }
        return new Err<>(new CompileException("Suffix '" + slice + "' not present", input));
    }

    private static Result<String, CompileException> split(
            String input,
            String infix,
            Function<String, Result<String, CompileException>> inLeft,
            Function<String, Result<String, CompileException>> inRight,
            BiFunction<String, String, String> merge
    ) {
        final var index = input.indexOf(infix);
        if (index == -1) return new Err<>(new CompileException("Infix '" + infix + "' not present", input));

        final var leftSlice = input.substring(0, index);
        final var rightSlice = input.substring(index + infix.length());

        return inLeft.apply(leftSlice)
                .flatMapValue(left -> inRight.apply(rightSlice)
                        .mapValue(right -> merge.apply(left, right)));
    }

    private static void advance(List<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
