package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var output = compile(input);
            final var target = source.resolveSibling("Main.c");
            Files.writeString(target, output);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String root) throws CompileException {
        final var segments = split(root);

        var output = new StringBuilder();
        for (String segment : segments) {
            output = output.append(compileRootMember(segment.strip()));
        }

        return output.toString();
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
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static String compileRootMember(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return "#include \"temp.h\"\n";
        return compileClass(rootSegment).orElseThrow(() -> new CompileException("Invalid root segment", rootSegment));
    }

    private static Optional<String> compileClass(String rootSegment) {
        return split(rootSegment, "class").flatMap(withoutClass -> {
            return split(withoutClass.right(), "{").flatMap(withoutContentStart -> {
                final var name = withoutContentStart.left().strip();
                final var withEnd = withoutContentStart.right();
                if (withEnd.endsWith("}")) {
                    final var content = withEnd.substring(0, "}".length());
                    //TODO
                    return Optional.of("struct " + name + " {};");
                } else {
                    return Optional.empty();
                }
            });
        });
    }

    private static Optional<Tuple<String, String>> split(String input, String slice) {
        final var index = input.indexOf(slice);
        if (index == -1) return Optional.empty();

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());
        return Optional.of(new Tuple<>(left, right));
    }
}
