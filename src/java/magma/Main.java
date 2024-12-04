package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.Error;
import magma.error.JavaError;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");

    public static void main(String[] args) {
        findSources()
                .mapValue(Main::runWithSources)
                .match(onOk -> onOk, Some::new)
                .ifPresent(e -> System.err.println(e.display()));
    }

    private static Result<List<Path>, magma.error.Error> findSources() {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(new JavaError(e));
        }
    }

    private static Option<magma.error.Error> runWithSources(List<Path> sources) {
        return Streams.from(sources)
                .map(Main::runWithSource)
                .flatMap(Streams::fromOption)
                .next();
    }

    private static Option<magma.error.Error> runWithSource(Path source) {
        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf('.');
        final var name = fileName.substring(0, separator);

        final var relativized = SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = Paths.get(".", "src", "magma").resolve(relativized);
        return createDirectories(targetParent).or(() -> {
            final var target = targetParent.resolve(name + ".mgs");

            return readSafe(source)
                    .flatMapValue(Main::compile)
                    .mapValue(output -> writeSafe(target, output))
                    .match(onOk -> onOk, Some::new);
        });
    }

    private static Option<Error> createDirectories(Path targetParent) {
        if (Files.exists(targetParent)) return new None<>();

        try {
            Files.createDirectories(targetParent);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new JavaError(e));
        }
    }

    private static Result<String, ApplicationError> compile(String input) {
        final var segments = split(input);

        return Streams.from(segments)
                .foldLeftIntoResult(new StringBuilder(), (builder, segment) -> compileRootMember(segment).mapValue(builder::append))
                .mapValue(StringBuilder::toString)
                .mapErr(ApplicationError::new);
    }

    private static Option<Error> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Result<String, ApplicationError> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static List<String> split(String input) {
        var state = new State();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            var appended = state.append(c);
            state = splitAtChar(appended, c);
        }

        return state.advance().segments();
    }

    private static State splitAtChar(State state, char c) {
        if (c == ';' && state.isLevel()) return state.advance();
        if (c == '{') return state.enter();
        if (c == '}') return state.exit();
        return state;
    }

    private static Result<String, CompileError> compileRootMember(String input) {
        if (input.startsWith("package ")) {
            if (input.endsWith(";")) {
                return new Ok<>("");
            }
        }

        final var result = compileImport(input);
        if(result.isOk()) return result;

        if (input.contains("record")) {
            return generateFunction();
        }

        if (input.contains("class")) {
            return generateFunction();
        }

        if (input.contains("interface")) {
            return new Ok<>("trait Temp {}");
        }

        return new Err<>(new CompileError("Invalid root member", input));
    }

    private static Result<String, CompileError> compileImport(String input) {
        final var stripped = input.strip();
        final var prefix = "import ";
        if (!stripped.startsWith(prefix))
            return new Err<>(new CompileError("Prefix '" + prefix + "' not present", input));

        final var withoutPrefix = stripped.substring(prefix.length());
        final var suffix = ";";
        if (!withoutPrefix.endsWith(suffix))
            return new Err<>(new CompileError("Suffix '" + suffix + "' not present", input));

        final var withoutSuffix = withoutPrefix.substring(0, withoutPrefix.length() - 1);
        return parse(withoutSuffix).flatMapValue(Main::generateImport);
    }

    private static Result<Node, CompileError> parse(String input) {
        return new Ok<>(new Node(input));
    }

    private static Ok<String, CompileError> generateImport(Node node) {
        return new Ok<>("import " + node.namespace() + ";");
    }

    private static Ok<String, CompileError> generateFunction() {
        return new Ok<>("class def Temp() => {}");
    }
}
