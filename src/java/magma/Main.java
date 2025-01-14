package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
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
        return split(rootSegment, "class ", modifiers -> Optional.of(""), afterKeyword -> {
            return split(afterKeyword, "{", name -> {
                return Optional.of(name.strip());
            }, withEnd -> {
                return truncateRight(withEnd.strip(), "}", content -> {
                    return Optional.of("");
                });
            }, (left, _) -> left + " {}");
        }, (left, right) -> left + "struct " + right);
    }

    private static Optional<String> truncateRight(String input, String slice, Function<String, Optional<String>> mapper) {
        if (input.endsWith(slice)) {
            return mapper.apply(input.substring(0, input.length() - slice.length()));
        }
        return Optional.empty();
    }

    private static Optional<String> split(
            String rootSegment,
            String infix,
            Function<String, Optional<String>> inLeft,
            Function<String, Optional<String>> inRight,
            BiFunction<String, String, String> merge
    ) {
        final var index = rootSegment.indexOf(infix);
        if (index == -1) return Optional.empty();

        final var leftSlice = rootSegment.substring(0, index);
        final var rightSlice = rootSegment.substring(index + infix.length());

        return inLeft.apply(leftSlice)
                .flatMap(left -> inRight.apply(rightSlice)
                        .map(right -> merge.apply(left, right)));
    }

    private static void advance(List<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
