package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = splitAtChar(state, c, queue);
        }

        if (state.isLevel()) {
            return new Ok<>(state.advance().segments());
        } else {
            return new Err<>(new CompileException("Invalid depth '" + state.depth() + "'", input));
        }
    }

    private static State splitAtChar(State state, char c, Deque<Character> queue) {
        final var appended = state.append(c);
        if (c == '\'') {
            final var maybeEscape = queue.pop();
            final var next = state.append(maybeEscape);
            final State next1;
            if (maybeEscape == '\\') {
                next1 = state.append(queue.pop());
            } else {
                next1 = next;
            }

            return next1.append(queue.pop());
        }

        if (c == '"') {
            var current = appended;
            while (!queue.isEmpty()) {
                final var next = queue.pop();
                current = current.append(next);
                if (next == '"') {
                    break;
                }
                if (next == '\\') {
                    current = current.append(queue.pop());
                }
            }

            return current;
        }

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
            return truncateRight(withEnd, "}")
                    .map(inputContent -> splitAndCompile(inputContent, Main::compileClassStatement)
                            .mapValue(outputContent -> "struct " + name + " {" + outputContent + "\n};"));
        }));
    }

    private static Optional<String> truncateRight(String input, String slice) {
        return input.endsWith(slice) ? Optional.of(input.substring(0, input.length() - slice.length())) : Optional.empty();
    }

    private static Result<String, CompileException> compileClassStatement(String classMember) {
        return compileMethod(classMember).orElseGet(() -> new Err<>(new CompileException("Invalid class member", classMember)));
    }

    private static Optional<Result<String, CompileException>> compileMethod(String classMember) {
        return split(classMember, "(", Main::locateFirst).flatMap(withoutParamStart -> {
            final var beforeParams = withoutParamStart.left();
            final var withParams = withoutParamStart.right();

            return split(withParams, "{").flatMap(tuple -> {
                final var withParamEnd = tuple.left().strip();
                return truncateRight(withParamEnd, ")").flatMap(params -> {
                    return split(beforeParams, " ", Main::locateLast).flatMap(withoutNameSeparator -> {
                        final var beforeName = withoutNameSeparator.left();
                        return split(beforeName, " ", Main::locateTypeSeparator).map(tuple0 -> {
                            final var type = tuple0.right();
                            final var name = withoutNameSeparator.right();
                            return "\n\t" + type + " " + name + "(" + params + "){}";
                        });
                    });
                });
            });
        }).map(Ok::new);
    }

    private static Optional<Tuple<String, String>> split(String withParams, String s) {
        return split(withParams, s, Main::locateFirst);
    }

    private static Optional<Integer> locateTypeSeparator(String input, String slice) {
        var depth = 0;
        for (int i = input.length() - 1; i >= 0; i--) {
            var c = input.charAt(i);
            if (c == ' ' && depth == 0) return Optional.of(i);
            if (c == '>') depth++;
            if (c == '<') depth--;
        }
        return Optional.empty();
    }

    private static Optional<Integer> locateLast(String input, String slice) {
        final var index = input.lastIndexOf(slice);
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
