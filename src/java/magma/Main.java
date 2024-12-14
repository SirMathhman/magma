package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

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
            output = output.and(() -> segmentCompiler.apply(segment))
                    .mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static List<String> split(String input) {
        return processChars(new MutableSplitState(), input)
                .advance()
                .asList();
    }

    private static SplitState processChars(SplitState initial, String input) {
        var current = initial;
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            final var appended = current.next(c);
            current = processChar(c, appended);
        }
        return current;
    }

    private static SplitState processChar(char c, SplitState state) {
        if (c == ';' && state.isLevel()) {
            return state.advance();
        }
        if (c == '}' && state.isShallow()) {
            return state.exit().advance();
        }
        if (c == '{') return state.enter();
        if (c == '}') return state.exit();
        return state;
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
        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return new None<>();

        final var contentEnd = input.lastIndexOf('}');
        if (contentEnd == -1) return new None<>();

        final var inputContent = input.substring(contentStart + 1, contentEnd).strip();
        final var outputContent = compileSegments(inputContent, Main::compileClassMember);
        return new Some<>(outputContent);
    }

    private static Result<String, CompileException> compileClassMember(String classMember) {
        return new Err<>(new CompileException("Unknown class member", classMember));
    }

}
