package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        final var sources = collectSources();
        compileSources(sources).ifPresent(error -> System.err.println(error.display()));
    }

    private static Optional<ApplicationError> compileSources(Set<Path> sources) {
        return sources.stream()
                .map(Main::compileSource)
                .flatMap(Options::stream)
                .findFirst();
    }

    private static Option<ApplicationError> compileSource(Path source) {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var nameWithExt = relativized.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(relativized.getParent());

        if (!Files.exists(targetParent)) {
            try {
                Files.createDirectories(targetParent);
            } catch (IOException e) {
                return new Some<>(new ApplicationError(new None<>(), new Some<>(new JavaError(e))));
            }
        }
        final var target = targetParent.resolve(name + ".mgs");

        final String input;
        try {
            input = Files.readString(source);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new None<>(), new Some<>(new JavaError(e))));
        }

        return compile(input).mapValue(value -> {
            try {
                Files.writeString(target, value);
                return new None<ApplicationError>();
            } catch (IOException e) {
                return new Some<>(new ApplicationError(new JavaError(e)));
            }
        }).mapErr(ApplicationError::new).match(value -> value, Some::new);
    }

    private static Result<String, ApplicationError> compile(String input) {
        return split(input)
                .stream()
                .map(String::strip)
                .map(Main::compileRootSegment)
                .<Result<StringBuilder, ApplicationError>>reduce(new Ok<>(new StringBuilder()),
                        (current, next) -> current.and(() -> next).mapValue(tuple -> tuple.left().append(tuple.right())),
                        (_, next) -> next)
                .mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        final var length = input.length();
        for (int i = 0; i < length; i++) {
            final var c = input.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Result<String, ApplicationError> compileRootSegment(String input) {
        if (input.startsWith("package ")) return new Ok<>("");
        if (input.startsWith("import ")) return new Ok<>(input);
        if (input.contains("class ")) return renderFunction();
        if (input.contains("record ")) return renderFunction();
        if (input.contains("interface ")) return new Ok<>("trait Temp {}");
        return new Err<>(ApplicationError.createContextError("Invalid root segment", input));
    }

    private static Ok<String, ApplicationError> renderFunction() {
        return new Ok<>("class def Temp() => {}");
    }

    private static Set<Path> collectSources() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
