package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.JavaError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        collect().mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(Function.identity(), Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Result<Set<Path>, IOException> collect() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<ApplicationError> runWithSources(Set<Path> sources) {
        for (Path source : sources) {
            final var error = runWithSource(source);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var nameWithExt = relative.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoriesError = createDirectoriesWrapped(targetParent);
            if (directoriesError.isPresent()) return directoriesError.map(JavaError::new).map(ApplicationError::new);
        }

        return readStringWrapped(source).mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(input -> {
            return compile(input).mapErr(ApplicationError::new).mapValue(output -> {
                final var target = targetParent.resolve(name + ".c");
                final var header = targetParent.resolve(name + ".h");
                return writeStringWrapped(target, output)
                        .or(() -> writeStringWrapped(header, output))
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }).match(Function.identity(), Optional::of);
        }).match(Function.identity(), Optional::of);
    }

    private static Result<String, IOException> readStringWrapped(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<IOException> createDirectoriesWrapped(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Optional<IOException> writeStringWrapped(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, CompileError> compile(String root) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            final var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);

        Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output.and(() -> compileRootSegment(segment)).mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Err<String, CompileError> compileRootSegment(String root) {
        return new Err<>(new CompileError("Invalid root", root));
    }
}
