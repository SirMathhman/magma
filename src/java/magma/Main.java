package magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.ApplicationException;
import magma.app.CompileException;
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
                .mapValue(input -> runWithInput(input, target))
                .match(value -> value, Some::new)
                .ifPresent(Throwable::printStackTrace);
    }

    private static Option<ApplicationException> runWithInput(String input, Path target) {
        return compile(input)
                .mapErr(ApplicationException::new)
                .mapValue(output -> writeSafe(output, target).map(ApplicationException::new))
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

    private static Result<String, CompileException> compile(String input) {
        return compileSegments(input, Main::compileRootSegment);
    }

    private static Result<String, CompileException> compileSegments(
            String input,
            Function<String, Result<String, CompileException>> segmentCompiler
    ) {
        final var segments = split(input);

        Result<StringBuilder, CompileException> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output.and(() -> segmentCompiler.apply(segment.strip()))
                    .mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static List<String> split(String input) {
        return processChars(input)
                .advance()
                .asList();
    }

    private static SplitState processChars(String input) {
        final var length = input.length();

        final var queue = IntStream.range(0, length)
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        SplitState state = new MutableSplitState(queue);
        while (true) {
            final var tuple = processChar(state)
                    .toTuple(state);
            if (tuple.left()) {
                state = tuple.right();
            } else {
                break;
            }
        }
        return state;
    }

    private static Option<SplitState> processChar(SplitState state) {
        return state.pop().map(tuple -> {
            final var c = tuple.left();
            final var current = tuple.right();
            return processPoppedChar(current, c);
        });
    }

    private static SplitState processPoppedChar(SplitState current, Character c) {
        final var appended = current.next(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static Result<String, CompileException> compileRootSegment(String segment) {
        final var stripped = segment.strip();

        return compilePackage(stripped, "package ")
                .or(() -> compilePackage(stripped, "import "))
                .or(() -> compileClass(stripped))
                .orElseGet(() -> new Err<>(new CompileException("Unknown root segment", segment)));
    }

    private static Option<Result<String, CompileException>> compilePackage(String stripped, String prefix) {
        return stripped.startsWith(prefix)
                ? new Some<>(new Ok<>(""))
                : new None<>();
    }

    private static Option<Result<String, CompileException>> compileClass(String input) {
        return compileBlock(input, Main::compileClassMember);
    }

    private static Result<String, CompileException> compileClassMember(String classMember) {
        return compileMethod(classMember).orElseGet(() -> new Err<>(new CompileException("Unknown class member", classMember)));
    }

    private static Option<Result<String, CompileException>> compileMethod(String input) {
        return compileBlock(input, Main::compileStatement);
    }

    private static Option<Result<String, CompileException>> compileBlock(String input, Function<String, Result<String, CompileException>> compiler) {
        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return new None<>();

        final var contentEnd = input.lastIndexOf('}');
        if (contentEnd == -1) return new None<>();

        final var inputContent = input.substring(contentStart + 1, contentEnd).strip();
        final var outputContent = compileSegments(inputContent, compiler);
        return new Some<>(outputContent);
    }

    private static Result<String, CompileException> compileStatement(String input) {
        return compileDefinition(input)
                .or(() -> compileInvocation(input))
                .or(() -> compileReturn(input))
                .orElseGet(() -> new Err<>(new CompileException("Unknown statement", input)));
    }

    private static Option<Result<String, CompileException>> compileReturn(String input) {
        if (input.startsWith("return ")) return new Some<>(new Ok<>("return 0;"));
        return new None<>();
    }

    private static Option<Result<String, CompileException>> compileInvocation(String input) {
        final var paramStart = input.lastIndexOf('(');
        if (paramStart == -1) return new None<>();

        final var paramEnd = input.lastIndexOf(')');
        if (paramEnd == -1) return new None<>();

        return new Some<>(new Ok<>("empty();"));
    }

    private static Option<Result<String, CompileException>> compileDefinition(String input) {
        final var separator = input.indexOf("=");
        if (separator == -1) {
            return new None<>();
        }

        return new Some<>(new Ok<>("let value = 0;"));
    }
}
