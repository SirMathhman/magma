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
import magma.compile.pass.Formatter;
import magma.compile.pass.Modifier;
import magma.compile.pass.TreePassingStage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        {

        }

        final Path source = Paths.get(".", "src", "java", "magma", "Main.java");
        JavaFiles.readString(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .match(input -> runWithInput(source, input), Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Optional<ApplicationError> runWithInput(Path source, String input) {
        return JavaLang.createJavaRootRule()
                .parse(input)
                .mapErr(ApplicationError::new)
                .flatMapValue(parsed -> writeInputAST(source, parsed))
                .mapValue(node -> new TreePassingStage(new Modifier()).pass(new State(), node).right())
                .mapValue(node -> new TreePassingStage(new Formatter()).pass(new State(), node).right())
                .flatMapValue(parsed -> CLang.createCRootRule().generate(parsed).mapErr(ApplicationError::new))
                .mapValue(generated -> writeGenerated(generated, source.resolveSibling("Main.c"))).match(value -> value, Optional::of);
    }

    private static Result<Node, ApplicationError> writeInputAST(Path source, Node parsed) {
        return JavaFiles.writeString(source.resolveSibling("Main.input.ast"), parsed.toString())
                .map(JavaError::new)
                .map(ApplicationError::new)
                .<Result<Node, ApplicationError>>map(Err::new)
                .orElseGet(() -> new Ok<>(parsed));
    }

    private static Optional<ApplicationError> writeGenerated(String generated, Path target) {
        return JavaFiles.writeString(target, generated)
                .map(JavaError::new)
                .map(ApplicationError::new);
    }
}
