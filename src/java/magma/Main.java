package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            runWithSources(sources, sourceDirectory).ifPresent(Throwable::printStackTrace);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Optional<ApplicationException> runWithSources(List<Path> sources, Path sourceDirectory) {
        for (Path source : sources) {
            final var error = runWithSource(sourceDirectory, source);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationException> runWithSource(Path sourceDirectory, Path source) {
        final var relativized = sourceDirectory.relativize(source);
        final var parent = relativized.getParent();
        final var targetDirectory = Paths.get(".", "src", "c");
        final var targetParent = targetDirectory.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoryCreationError = createDirectoriesSafe(targetParent);
            if (directoryCreationError.isPresent()) {
                return directoryCreationError.map(ApplicationException::new);
            }
        }

        final var name = relativized.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);

        return readSafe(source).mapErr(ApplicationException::new).mapValue(input -> {
            return compileRoot(input).mapErr(ApplicationException::new).mapValue(output -> {
                final var target = targetParent.resolve(nameWithoutExt + ".c");
                return writeSafe(output, target).map(ApplicationException::new);
            }).match(value -> value, Optional::of);
        }).match(value -> value, err -> Optional.of(err));
    }

    private static Optional<IOException> writeSafe(String output, Path target) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, IOException> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<IOException> createDirectoriesSafe(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, CompileException> compileRoot(String root) {
        final var segments = split(root);
        Result<StringBuilder, CompileException> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output
                    .and(() -> compileRootSegment(segment.strip()))
                    .mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static List<String> split(String root) {
        var state = new State();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments;
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static Result<String, CompileException> compileRootSegment(String segment) {
        if (segment.startsWith("package ")) return new Ok<>("");
        if (segment.startsWith("import ")) return new Ok<>("#include \"temp.h\";\n");
        if (segment.contains("class ")) return new Ok<>("struct Temp {};");
        return new Err<>(new CompileException("Unknown root segment", segment));
    }
}
