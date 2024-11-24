package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
        return compilePackage(input)
                .or(() -> compileImport(input))
                .or(() -> compileClass(input))
                .or(() -> compileRecord(input))
                .or(() -> compileInterface(input))
                .orElseGet(() -> new Err<>(ApplicationError.createContextError("Invalid root segment", input)));
    }

    private static Option<Result<String, ApplicationError>> compileImport(String input) {
        if (input.startsWith("import ")) return new Some<>(new Ok<>(input));
        return new None<>();
    }

    private static Option<Result<String, ApplicationError>> compilePackage(String input) {
        if (input.startsWith("package ")) return new Some<>(new Ok<>(""));
        return new None<>();
    }

    private static Option<Result<String, ApplicationError>> compileRecord(String input) {
        if (input.contains("record ")) return new Some<>(renderFunction(Collections.emptyList(), "Temp", ""));
        return new None<>();
    }

    private static Option<Result<String, ApplicationError>> compileInterface(String input) {
        if (input.contains("interface ")) return new Some<>(new Ok<>("trait Temp {}"));
        return new None<>();
    }

    private static Option<Result<String, ApplicationError>> compileClass(String input) {
        final var classIndex = input.indexOf("class ");
        if (classIndex == -1) return new None<>();

        final var contentStart = input.indexOf('{');
        final var modifiersArray = input.substring(0, classIndex).strip().split(" ");
        final var oldModifiers = Arrays.stream(modifiersArray)
                .map(String::strip)
                .filter(modifier -> !modifier.isEmpty())
                .toList();

        var newModifiers = new ArrayList<String>();
        if (oldModifiers.contains("public")) {
            newModifiers.add("export");
        }

        final var nameAndMaybeImplements = input.substring(classIndex + "class ".length(), contentStart).strip();

        final Result<String, ApplicationError> rendered;
        final var implementsIndex = nameAndMaybeImplements.indexOf("implements ");
        if (implementsIndex == -1) {
            rendered = renderFunction(newModifiers, nameAndMaybeImplements, "");
        } else {
            final var name = nameAndMaybeImplements.substring(0, implementsIndex).strip();
            final var type = nameAndMaybeImplements.substring(implementsIndex + "implements ".length()).strip();
            rendered = renderFunction(newModifiers, name, "\n\timplements " + type + ";");
        }

        return new Some<>(rendered);
    }

    private static Result<String, ApplicationError> renderFunction(List<String> newModifiers, String name, String content) {
        final var copy = new ArrayList<>(newModifiers);
        copy.add("class");
        final var joinedModifiers = String.join(" ", copy);
        return new Ok<>(joinedModifiers + " def " + name + "() => {" + content + "\n}");
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
