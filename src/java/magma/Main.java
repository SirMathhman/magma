package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            runWithSources(sources, sourceDirectory).ifPresent(applicationError -> System.err.println(applicationError.display()));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Optional<ApplicationError> runWithSources(List<Path> sources, Path sourceDirectory) {
        for (Path source : sources) {
            final var error = runWithSource(sourceDirectory, source);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Path sourceDirectory, Path source) {
        final var relativized = sourceDirectory.relativize(source);
        final var parent = relativized.getParent();
        final var targetDirectory = Paths.get(".", "src", "c");
        final var targetParent = targetDirectory.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoryCreationError = createDirectoriesSafe(targetParent);
            if (directoryCreationError.isPresent()) {
                return directoryCreationError
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }
        }

        final var name = relativized.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);

        return readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> compileInputToTarget(input, targetParent, nameWithoutExt))
                .match(value -> value, Optional::of);
    }

    private static Optional<ApplicationError> compileInputToTarget(String input, Path targetParent, String nameWithoutExt) {
        return compileRoot(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeOutputToTarget(targetParent, nameWithoutExt, output))
                .match(value -> value, Optional::of);
    }

    private static Optional<ApplicationError> writeOutputToTarget(Path targetParent, String nameWithoutExt, String output) {
        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return writeSafe(output, target)
                .map(JavaError::new)
                .map(ApplicationError::new);
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

    private static Result<String, CompileError> compileRoot(String root) {
        return compileSegments(root, Main::compileRootSegment);
    }

    private static Result<String, CompileError> compileSegments(String root, Function<String, Result<String, CompileError>> mapper) {
        final var segments = split(root);
        Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output
                    .and(() -> mapper.apply(segment.strip()))
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
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static Result<String, CompileError> compileRootSegment(String segment) {
        return compilePackage(segment)
                .or(() -> compileImport(segment))
                .or(() -> compileToStruct(segment, "class "))
                .or(() -> compileToStruct(segment, "record "))
                .or(() -> compileToStruct(segment, "interface "))
                .orElseGet(() -> new Err<>(new CompileError("Unknown root segment", segment)));
    }

    private static Optional<? extends Result<String, CompileError>> compileToStruct(String segment, String keyword) {
        final var keywordIndex = segment.indexOf(keyword);
        if (keywordIndex == -1) return Optional.empty();

        final var contentStart = segment.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var contentEnd = segment.lastIndexOf('}');
        if (contentEnd == -1) return Optional.empty();

        final var content = segment.substring(contentStart + 1, contentEnd);
        final var outputResult = compileSegments(content, Main::compileStructMember);

        final var maybeImplements = segment.substring(keywordIndex + keyword.length(), contentStart);
        String name;
        final var implementsIndex = maybeImplements.indexOf("implements ");
        if (implementsIndex != -1) {
            name = maybeImplements.substring(0, implementsIndex).strip();
        } else {
            name = maybeImplements;
        }

        return Optional.of(outputResult.mapValue(output -> "struct " + name + " {" + output + "\n};"));
    }

    private static Result<String, CompileError> compileStructMember(String structMember) {
        if (structMember.endsWith(";")) {
            final var slice = structMember.substring(0, structMember.length() - 1);
            final var space = slice.lastIndexOf(' ');
            if (space != -1) {
                final var name = slice.substring(space + 1);
                return new Ok<>("\n\tint " + name + ";");
            }
        }

        return new Err<>(new CompileError("Unknown struct member", structMember));
    }

    private static Optional<? extends Result<String, CompileError>> compileImport(String segment) {
        return segment.startsWith("import ") ? Optional.of(new Ok<>("#include \"temp.h\";\n")) : Optional.empty();
    }

    private static Optional<Result<String, CompileError>> compilePackage(String segment) {
        return segment.startsWith("package ") ? Optional.of(new Ok<>("")) : Optional.empty();
    }
}
