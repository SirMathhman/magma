package magma;

import magma.error.CompileError;
import magma.error.JavaError;
import magma.java.JavaCollections;
import magma.result.ApplicationError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");

    public static void main(String[] args) {
        walk().mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(Function.identity(), Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Optional<ApplicationError> runWithSources(Set<Path> paths) {
        final var sources = paths.stream()
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toSet());

        for (Path source : sources) {
            final var error = runWithSource(source);
            if (error.isPresent()) return error;
        }

        return Optional.empty();
    }

    private static Result<Set<Path>, IOException> walk() {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            return new Ok<>(stream.collect(Collectors.toSet()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<ApplicationError> runWithSource(Path source) {
        final var relativized = Main.SOURCE_DIRECTORY.relativize(source);
        final var parent = relativized.getParent();

        final var targetDirectory = Paths.get(".", "src", "c");
        final var targetParent = targetDirectory.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var error = createDirectoriesSafe(targetParent);
            if (error.isPresent()) return error;
        }

        final var name = relativized.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));

        return readStringSafe(source).mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(input -> {
            return compileRoot(input).mapErr(ApplicationError::new).mapValue(output -> {
                final var header = targetParent.resolve(nameWithoutExt + ".h");
                final var target = targetParent.resolve(nameWithoutExt + ".c");
                return writeStringSafe(output, header)
                        .or(() -> writeStringSafe(output, target))
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }).match(Function.identity(), Optional::of);
        }).match(Function.identity(), Optional::of);
    }

    private static Optional<ApplicationError> createDirectoriesSafe(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(new ApplicationError(new JavaError(e)));
        }
    }

    private static Result<String, IOException> readStringSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<IOException> writeStringSafe(String output, Path header) {
        try {
            Files.writeString(header, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, CompileError> compileRoot(String root) {
        final var segments = split(root);

        Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            output = output
                    .and(() -> compileRootSegment(segment.strip()))
                    .mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String root) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            final var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        advance(buffer, segments);
        return segments;
    }

    private static Result<String, CompileError> compileRootSegment(String rootSegment) {
        final var suppliers = List.<Supplier<Result<String, CompileError>>>of(
                () -> compilePackage(rootSegment),
                () -> compileImport(rootSegment),
                () -> compileToStruct(rootSegment, "class "),
                () -> compileToStruct(rootSegment, "interface "),
                () -> compileToStruct(rootSegment, "record ")
        );

        return Streams.from(suppliers)
                .foldLeft(
                        first -> first.get().mapErr(Collections::singletonList),
                        (current, next) -> current.or(next).mapErr(Tuple.merge(JavaCollections::foldList)))
                .orElse(new Err<>(Collections.singletonList(new CompileError("No rules present", rootSegment))))
                .mapErr(errors -> new CompileError("Invalid root segment", rootSegment, errors));
    }

    private static Result<String, CompileError> compileImport(String input) {
        if (input.startsWith("import ")) return new Ok<>("#include \"temp.h\"\n");
        return new Err<>(new CompileError("No prefix 'import ' present", input));
    }

    private static Result<String, CompileError> compileToStruct(String rootSegment, String infix) {
        if (rootSegment.contains(infix)) return new Ok<>("struct Temp {\n};");
        return new Err<>(new CompileError("Infix '" + infix + "' not present", rootSegment));
    }

    private static Result<String, CompileError> compilePackage(String input) {
        if (input.startsWith("package ")) return new Ok<>("");
        return new Err<>(new CompileError("No prefix 'package ' present", input));
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
