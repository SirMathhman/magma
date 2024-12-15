package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.ApplicationError;
import magma.app.CompileError;
import magma.app.MutableSplitState;
import magma.app.SplitState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var target = source.resolveSibling("Main.mgs");
        readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> runWithInput(input, target))
                .match(value -> value, Some::new)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Option<ApplicationError> runWithInput(String input, Path target) {
        return compile(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeSafe(output, target)
                        .map(JavaError::new)
                        .map(ApplicationError::new))
                .match(value -> value, Some::new);
    }

    private static Option<IOException> writeSafe(String output, Path target) {
        try {
            Files.writeString(target, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }

    private static Result<String, IOException> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Result<String, CompileError> compile(String input) {
        return compileSegments(input, Main::compileRootSegment);
    }

    private static Result<String, CompileError> compileSegments(String input, Function<String, Result<String, CompileError>> segmentCompiler) {
        final var segments = split(input);

        Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output.and(() -> segmentCompiler.apply(segment.strip())).mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static List<String> split(String input) {
        return processChars(input).advance().asList();
    }

    private static SplitState processChars(String input) {
        final var length = input.length();

        final var queue = IntStream.range(0, length)
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        SplitState state = new MutableSplitState(queue);
        while (true) {
            final var tuple = processNextChar(state).toTuple(state);
            if (tuple.left()) {
                state = tuple.right();
            } else {
                break;
            }
        }
        return state;
    }

    private static Option<SplitState> processNextChar(SplitState state) {
        return state.pop()
                .map(tuple -> tuple.merge(SplitState::appendNext))
                .map(tuple -> tuple.merge(Main::processChar));
    }

    private static SplitState processChar(SplitState state, Character c) {
        if (c == ';' && state.isLevel()) return state.advance();
        if (c == '}' && state.isShallow()) return state.exit().advance();
        if (c == '{' || c == '(') return state.enter();
        if (c == '}' || c == ')') return state.exit();
        return state;
    }

    private static Result<String, CompileError> compileRootSegment(String segment) {
        final var stripped = segment.strip();

        return compilePackage(stripped, "package ")
                .or(() -> compilePackage(stripped, "import "))
                .or(() -> compileClass(stripped))
                .orElseGet(() -> new Err<>(new CompileError("Unknown root segment", new StringContext(segment))));
    }

    private static Option<Result<String, CompileError>> compilePackage(String stripped, String prefix) {
        return stripped.startsWith(prefix) ? new Some<>(new Ok<>("")) : new None<>();
    }

    private static Option<Result<String, CompileError>> compileClass(String input) {
        return compileBlock(input, Main::compileClassMember);
    }

    private static Result<String, CompileError> compileClassMember(String classMember) {
        return compileMethod(classMember)
                .map(result -> result.mapErr(err -> new CompileError("Invalid class member", new StringContext(classMember), err)))
                .orElseGet(() -> new Err<>(new CompileError("Unknown class member", new StringContext(classMember))));
    }

    private static Option<Result<String, CompileError>> compileMethod(String input) {
        return compileBlock(input, Main::compileStatement);
    }

    private static Option<Result<String, CompileError>> compileBlock(String input, Function<String, Result<String, CompileError>> compiler) {
        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return new None<>();

        final var contentEnd = input.lastIndexOf('}');
        if (contentEnd == -1) return new None<>();

        final var inputContent = input.substring(contentStart + 1, contentEnd).strip();
        final var outputContent = compileSegments(inputContent, compiler);
        return new Some<>(outputContent);
    }

    private static Result<String, CompileError> compileStatement(String input) {
        return compileDefinition(input)
                .or(() -> compileInvocationStatement(input))
                .or(() -> compileReturn(input))
                .orElseGet(() -> new Err<>(new CompileError("Unknown statement", new StringContext(input))));
    }

    private static Option<Result<String, CompileError>> compileReturn(String input) {
        if (input.startsWith("return ")) return new Some<>(new Ok<>("return 0;"));
        return new None<>();
    }

    private static Option<Result<String, CompileError>> compileInvocationStatement(String input) {
        final var paramStart = input.lastIndexOf('(');
        if (paramStart == -1) return new None<>();

        final var paramEnd = input.lastIndexOf(')');
        if (paramEnd == -1) return new None<>();

        return new Some<>(new Ok<>("empty();"));
    }

    private static Option<Result<String, CompileError>> compileDefinition(String input) {
        final var stripped = input.strip();

        final var valueSeparator = stripped.indexOf("=");
        if (valueSeparator == -1) return new None<>();

        if (!stripped.endsWith(";")) return new None<>();
        final var value = stripped.substring(valueSeparator + 1, stripped.length() - 1);

        return new Some<>(compileValue(value));
    }

    private static Result<String, CompileError> compileValue(String value) {
        return compileConstruction(value)
                .or(() -> compileInvocation(value))
                .or(() -> compileAccess(value))
                .or(() -> compileSymbol(value))
                .orElseGet(() -> new Err<>(new CompileError("Unknown value", new StringContext(value))));
    }

    private static Option<Result<String, CompileError>> compileConstruction(String value) {
        final var stripped = value.strip();
        if (stripped.startsWith("new ")) {
            final var endIndex = stripped.indexOf('(');
            if (endIndex != -1) {
                final var withoutNew = stripped.substring("new ".length(), endIndex);
                final var genStart = withoutNew.indexOf('<');
                if (genStart != -1) {
                    final var caller = withoutNew.substring(0, genStart);
                    return new Some<>(compileValue(caller));
                }
            }
        }
        return new None<>();
    }

    private static Option<Result<String, CompileError>> compileSymbol(String value) {
        final var stripped = value.strip();
        for (int i = 0; i < stripped.length(); i++) {
            var c = stripped.charAt(i);
            if (!Character.isLetter(c)) {
                return new None<>();
            }
        }

        return new Some<>(generateString(new MapNode().withString("value", stripped)));
    }

    private static Result<String, CompileError> generateString(MapNode mapNode) {
        return mapNode.findString("value")
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("String '" + "value" + "' not present", new NodeContext(mapNode))));
    }

    private static Option<Result<String, CompileError>> compileAccess(String value) {
        final var separator = value.indexOf('.');
        if (separator == -1) return new None<>();

        final var caller = value.substring(0, separator);
        return new Some<>(compileValue(caller));
    }

    private static Option<Result<String, CompileError>> compileInvocation(String value) {
        final var paramStart = value.lastIndexOf('(');
        if (paramStart == -1) return new None<>();

        final var caller = value.substring(0, paramStart);
        return new Some<>(compileValue(caller));
    }
}
