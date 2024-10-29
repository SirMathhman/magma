package magma;

import magma.java.Path_;
import magma.java.JavaPath;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.io.IOException;
import java.util.function.Function;

public final class Application {
    public static final String MAGMA_EXTENSION = JavaPath.EXTENSION_SEPARATOR + "mgs";
    private final Path_ source;

    public Application(Path_ source) {
        this.source = source;
    }

    private static Option<IOException> writeOutput(Path_ source, String output) {
        final var nameWithoutExtension = source.computeFileNameWithoutExtension();
        final var targetName = nameWithoutExtension + MAGMA_EXTENSION;
        final var target = source.resolveSibling(targetName);
        return target.writeSafe(output);
    }

    private static String compile(String input) {
        if (input.equals(renderImport())) {
            return input;
        } else {
            return "";
        }
    }

    static String renderImport() {
        return "import magma;";
    }

    Option<IOException> run() {
        if (!source.exists()) return new None<>();

        return source.readString()
                .mapValue(Application::compile)
                .mapValue(output -> writeOutput(source, output))
                .match(Function.identity(), Some::new);
    }
}