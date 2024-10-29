package magma;

import magma.compile.Compiler;
import magma.core.String_;
import magma.core.io.Path_;
import magma.core.option.None;
import magma.core.option.Option;
import magma.core.option.Some;
import magma.core.result.Result;
import magma.java.error.JavaError;
import magma.java.io.JavaPath;

import java.util.function.Function;

public final class Application {
    public static final String MAGMA_EXTENSION = JavaPath.EXTENSION_SEPARATOR + "mgs";
    private final Path_ source;

    public Application(Path_ source) {
        this.source = source;
    }

    private static Option<ApplicationError> writeOutput(Path_ source, String_ output) {
        final var nameWithoutExtension = source.computeFileNameWithoutExtension();
        final var targetName = nameWithoutExtension.appendSlice(MAGMA_EXTENSION);
        final var target = source.resolveSibling(targetName);

        return target.writeSafe(output)
                .map(JavaError::new)
                .map(ApplicationError::new);
    }

    private static Result<String_, ApplicationError> compileAndWrap(Compiler compiler) {
        return compiler.compile().mapErr(ApplicationError::new);
    }

    Option<ApplicationError> run() {
        if (!source.exists()) return new None<>();

        return source.readString()
                .mapErr(error -> new ApplicationError(new JavaError(error)))
                .mapValue(Compiler::new)
                .flatMapValue(Application::compileAndWrap)
                .mapValue(output -> writeOutput(source, output))
                .match(Function.identity(), Some::new);
    }
}