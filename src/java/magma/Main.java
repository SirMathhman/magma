package magma;

import magma.error.Error;
import magma.error.*;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.rule.*;
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
        return new OrRule(List.of(
                createNamespaceRule("package", "package "),
                createImportRule(),
                new InfixRule("record"),
                new InfixRule("class"),
                new InfixRule("interface")
        )).parse(input).flatMapValue(node -> {
            return new OrRule(List.of(
                    createImportRule(),
                    createFunctionRule(),
                    createTraitRule()
            )).generate(node);
        });
    }

    private static ExactRule createFunctionRule() {
        return new ExactRule("class def Temp() => {}");
    }

    private static ExactRule createTraitRule() {
        return new ExactRule("trait Temp {}");
    }

    private static Rule createImportRule() {
        return createNamespaceRule("import", "import ");
    }

    private static Rule createNamespaceRule(String type, String prefix) {
        return new TypeRule(type, new StripRule(new PrefixRule(prefix, new SuffixRule(new StringRule(Node.NAMESPACE_VALUE), ";"))));
    }
}
