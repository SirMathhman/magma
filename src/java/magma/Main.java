package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
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
        return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileException> outputResult = new Ok<>(new StringBuilder());
            for (String segment : segments) {
                final var stripped = segment.strip();
                if (stripped.isEmpty()) continue;

                outputResult = outputResult
                        .and(() -> compiler.apply(stripped))
                        .mapValue(tuple -> tuple.left().append(tuple.right()));
            }

            return outputResult.mapValue(StringBuilder::toString);
        });
    }

    private static Result<List<String>, CompileException> split(String input) {
        var state = new State();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            state = splitAtChar(state, c);
        }

        if (state.isLevel()) {
            return new Ok<>(state.advance().segments());
        } else {
            return new Err<>(new CompileException("Invalid depth '" + state.depth() + "'", input));
        }
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
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
        return split(rootSegment, "class", Main::locateFirst).flatMap(withoutClass -> split(withoutClass.right(), "{", Main::locateFirst).flatMap(withoutContentStart -> {
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
        return compileMethod(classMember).orElseGet(() -> new Err<>(new CompileException("Invalid class member", classMember)));
    }

    private static Optional<Result<String, CompileException>> compileMethod(String classMember) {
        return split(classMember, "(", Main::locateFirst).flatMap(withoutParamStart -> {
            return split(withoutParamStart.left(), " ", Main::locateLast).map(withoutNameSeparator -> {
                final var name = withoutNameSeparator.right();
                return "void " + name + "(){}";
            });
        }).map(Ok::new);
    }

    private static Optional<Integer> locateLast(String input, String slice) {
        final var index = input.indexOf(slice);
        return index == -1 ? Optional.empty() : Optional.of(index);
    }

    private static Optional<Tuple<String, String>> split(String input, String slice, BiFunction<String, String, Optional<Integer>> locator) {
        return locator.apply(input, slice).map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + slice.length());
            return new Tuple<>(left, right);
        });
    }

    private static Optional<Integer> locateFirst(String input, String slice) {
        final var index = input.indexOf(slice);
        return index == -1 ? Optional.empty() : Optional.of(index);
    }
}
