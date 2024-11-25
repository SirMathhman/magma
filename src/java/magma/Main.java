package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.JavaError;
import magma.option.None;
import magma.option.Option;
import magma.option.Options;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        extracted().ifPresent(error -> System.err.println(error.display()));
    }

    private static Option<ApplicationError> extracted() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .map(Main::compile)
                    .flatMap(Options::stream)
                    .findFirst()
                    .<Option<ApplicationError>>map(Some::new)
                    .orElseGet(None::new);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> compile(Path sourceFile) {
        return readString(sourceFile)
                .mapValue(input -> compileWithInput(sourceFile, input))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> compileWithInput(Path sourceFile, String input) {
        final var relativized = Main.SOURCE_DIRECTORY.relativize(sourceFile);
        final var parent = relativized.getParent();

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        return createDirectories(targetParent)
                .or(() -> compileWithTargetParent(sourceFile, targetParent, input));
    }

    private static Option<ApplicationError> compileWithTargetParent(Path sourceFile, Path targetParent, String input) {
        final var fileName = sourceFile.getFileName().toString();
        final var separator = fileName.indexOf('.');
        var name = fileName.substring(0, separator);
        final var target = targetParent.resolve(name + ".mgs");

        return compileRoot(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeOutput(target, output))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeOutput(Path file, String output) {
        try {
            Files.writeString(file, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> createDirectories(Path directories) {
        if (Files.exists(directories)) return new None<>();

        try {
            Files.createDirectories(directories);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, ApplicationError> readString(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return split(root)
                .stream()
                .map(Main::compileRootSegment)
                .<Result<StringBuilder, CompileError>>reduce(new Ok<>(new StringBuilder()), (stringBuilderCompileErrorResult, stringCompileErrorErr) -> stringBuilderCompileErrorResult.and(() -> stringCompileErrorErr).mapValue(tuple -> {
                    return tuple.left().append(tuple.right());
                }), (_, next) -> next)
                .mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        final var length = root.length();
        for (int i = 0; i < length; i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static Err<String, CompileError> compileRootSegment(String root) {
        return new Err<>(new CompileError("Invalid root segment", root));
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
