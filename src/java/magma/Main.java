package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var output = Results.unwrap(compile(input));
            final var target = source.resolveSibling("Main.c");
            Files.writeString(target, output);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Result<String, CompileException> compile(String root) {
        return splitAndCompile(root, Main::compileRootMember);
    }

    private static Result<String, CompileException> splitAndCompile(String root, Function<String, Result<String, CompileException>> compiler) {
        final var segments = split(root);

        Result<StringBuilder, CompileException> outputResult = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            outputResult = outputResult
                    .and(() -> compiler.apply(stripped))
                    .mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return outputResult.mapValue(StringBuilder::toString);
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

    private static Result<String, CompileException> compileRootMember(String rootSegment) {
        if (rootSegment.startsWith("package ")) return new Ok<>("");
        if (rootSegment.startsWith("import ")) return new Ok<>("#include \"temp.h\"\n");
        return compileClass(rootSegment).orElseGet(() -> new Err<>(new CompileException("Invalid root segment", rootSegment)));
    }

    private static Optional<Result<String, CompileException>> compileClass(String rootSegment) {
        return split(rootSegment, "class").flatMap(withoutClass -> split(withoutClass.right(), "{").flatMap(withoutContentStart -> {
            final var name = withoutContentStart.left().strip();
            final var withEnd = withoutContentStart.right();
            if (withEnd.endsWith("}")) {
                final var inputContent = withEnd.substring(0, withEnd.length() - "}".length());
                return Optional.of(splitAndCompile(inputContent, Main::compileClassStatement).mapValue(outputContent -> "struct " + name + " {" + outputContent + "};"));
            } else {
                return Optional.empty();
            }
        }));
    }

    private static Result<String, CompileException> compileClassStatement(String classMember) {
        return new Err<>(new CompileException("Invalid class member", classMember));
    }

    private static Optional<Tuple<String, String>> split(String input, String slice) {
        final var index = input.indexOf(slice);
        if (index == -1) return Optional.empty();

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());
        return Optional.of(new Tuple<>(left, right));
    }
}
