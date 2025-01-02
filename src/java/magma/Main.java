package magma;

import magma.java.JavaFiles;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.State;
import magma.compile.error.ApplicationError;
import magma.compile.error.JavaError;
import magma.compile.lang.CLang;
import magma.compile.lang.JavaLang;
import magma.compile.pass.Flattener;
import magma.compile.pass.Flattener0;
import magma.compile.pass.Generator;
import magma.compile.pass.Modifier;
import magma.compile.pass.TreePassingStage;
import magma.io.Source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main {

    public static final Path DEBUG = Paths.get(".", "debug");
    public static final Path TARGET = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        final var root = Paths.get(".", "src", "java");
        try (var stream = Files.walk(root)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .map(path -> new Source(root, path))
                    .filter(source -> !source.startsWithNamespace(List.of("magma", "java")))
                    .toList();

            runWithSources(sources).ifPresent(e -> System.err.println(e.display()));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Optional<ApplicationError> runWithSources(List<Source> sources) {
        for (var source : sources) {
            final var option = runWithSource(source);

            if (option.isPresent()) return option;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Source source) {
        return source.read()
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .match((input) -> runWithInput(source, input), Optional::of);
    }

    private static Optional<ApplicationError> runWithInput(Source source, String input) {
        final var generator = new Generator();
        return JavaLang.createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .flatMapValue((parsed) -> writeAST(source.resolve(DEBUG, ".input.ast"), parsed))
                .mapValue((node) -> new TreePassingStage<>(new Flattener(generator)).pass(new State(), node).right())
                .mapValue((node) -> new TreePassingStage<>(new Modifier(generator)).pass(new State(), node).right())
                .mapValue((node) -> new TreePassingStage<>(new Flattener0(generator)).pass(new State(), node).right())
                .flatMapValue((parsed) -> writeAST(source.resolve(DEBUG, ".output.ast"), parsed))
                .flatMapValue((parsed) -> CLang.createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue((generated) -> writeGenerated(source.resolve(TARGET, ".mgs"), generated))
                .match((value) -> value, Optional::of);
    }

    private static Result<Node, ApplicationError> writeAST(Path path, Node node) {
        try {
            Files.createDirectories(path.getParent());
            return JavaFiles.writeString(path, node.toString())
                    .map(JavaError::new)
                    .map(ApplicationError::new)
                    .<Result<Node, ApplicationError>>map(Err::new)
                    .orElseGet(() -> new Ok<>(node));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError(e)));
        }
    }

    private static Optional<ApplicationError> writeGenerated(Path target, String generated) {
        try {
            Files.createDirectories(target.getParent());
            return JavaFiles.writeString(target, generated)
                    .map(JavaError::new)
                    .map(ApplicationError::new);
        } catch (IOException e) {
            return Optional.of(new ApplicationError(new JavaError(e)));
        }
    }
}
