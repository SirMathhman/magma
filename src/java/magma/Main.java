package magma;

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
    public static void main(String[] args) {
        findSources()
                .mapValue(Main::runWithSources)
                .ifPresent(e -> System.err.println(e.display()));
    }

    private static Result<List<Path>, Error> findSources() {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(new JavaError(e));
        }
    }

    private static Option<Error> runWithSources(List<Path> sources) {
        return Streams.from(sources)
                .map(Main::runWithSource)
                .flatMap(Streams::fromOption)
                .next();
    }

    private static Option<Error> runWithSource(Path source) {
        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf('.');
        final var name = fileName.substring(0, separator);
        final var target = source.resolveSibling(name + ".mgs");

        return readSafe(source)
                .flatMapValue(Main::compile)
                .mapValue(output -> writeSafe(target, output))
                .match(onOk -> onOk, Some::new);
    }

    private static Result<String, ApplicationError> compile(String input) {
        final var segments = split(input);

        return Streams.from(segments)
                .foldLeftIntoResult(new StringBuilder(), (builder, segment) -> compileRootMember(segment).mapValue(builder::append))
                .mapValue(StringBuilder::toString)
                .mapErr(JavaError::new)
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

    private static Result<String, CompileException> compileRootMember(String input) {
        if (input.startsWith("package ")) {
            if (input.endsWith(";")) {
                return new Ok<>("");
            }
        }

        return new Err<>(new CompileException(input));
    }
}
