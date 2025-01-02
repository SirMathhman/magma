package magma;

import magma.api.JavaFiles;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        try (var stream = Files.walk(Paths.get(".", "src", "java"))) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            runWithSources(sources).ifPresent(e -> System.err.println(e.display()));
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Optional<ApplicationError> runWithSources(List<Path> sources) {
        for (var source : sources) {
            final var option = JavaFiles.readString(source)
                    .mapErr(JavaError::new)
                    .mapErr(ApplicationError::new)
                    .match((input) -> runWithInput(source, input), Optional::of);

            if (option.isPresent()) return option;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithInput(Path source, String input) {
        final var generator = new Generator();
        return JavaLang.createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .flatMapValue((parsed) -> writeAST(source.resolveSibling("Main.input.ast"), parsed))
                .mapValue((node) -> new TreePassingStage<>(new Flattener(generator)).pass(new State(), node).right())
                .mapValue((node) -> new TreePassingStage<>(new Modifier(generator)).pass(new State(), node).right())
                .mapValue((node) -> new TreePassingStage<>(new Flattener0(generator)).pass(new State(), node).right())
                .flatMapValue((parsed) -> writeAST(source.resolveSibling("Main.output.ast"), parsed))
                .flatMapValue((parsed) -> CLang.createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue((generated) -> writeGenerated(generated, source.resolveSibling("Main.c")))
                .match((value) -> value, Optional::of);
    }

    private static Result<Node, ApplicationError> writeAST(Path path, Node node) {
        return JavaFiles.writeString(path, node.toString())
                .map(JavaError::new)
                .map(ApplicationError::new)
                .<Result<Node, ApplicationError>>map(Err::new)
                .orElseGet(() -> new Ok<>(node));
    }

    private static Optional<ApplicationError> writeGenerated(String generated, Path target) {
        return JavaFiles.writeString(target, generated)
                .map(JavaError::new)
                .map(ApplicationError::new);
    }

}
