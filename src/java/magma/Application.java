package magma;

import magma.compile.Compiler;
import magma.core.String_;
import magma.core.io.Path_;
import magma.core.option.None;
import magma.core.option.Option;
import magma.core.option.Some;
import magma.java.JavaString;
import magma.java.io.JavaPath;

import java.io.IOException;
import java.util.function.Function;

public final class Application {
    public static final String MAGMA_EXTENSION = JavaPath.EXTENSION_SEPARATOR + "mgs";
    private final Path_ source;

    public Application(Path_ source) {
        this.source = source;
    }

    private static Option<IOException> writeOutput(Path_ source, String_ output) {
        final var nameWithoutExtension = source.computeFileNameWithoutExtension();
        final var targetName = nameWithoutExtension.appendSlice(MAGMA_EXTENSION);
        final var target = source.resolveSibling(targetName);
        return target.writeSafe(output);
    }

    Option<IOException> run() {
        if (!source.exists()) return new None<>();

        return source.readString()
                .mapValue(Compiler::new)
                .mapValue(compiler -> compiler.compile()
                        .findValue()
                        .orElse(JavaString.EMPTY))
                .mapValue(output -> writeOutput(source, output))
                .match(Function.identity(), Some::new);
    }
}