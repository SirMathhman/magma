package magma.app;

import magma.app.compile.Compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public final class Application {
    public static final String MAGMA_EXTENSION = "mgs";
    public static final String EXTENSION_SEPARATOR = ".";
    private final SourceSet sourceSet;

    public Application(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
    }

    private static void writeSafe(Path target, String output) throws ApplicationException {
        try {
            Files.writeString(target, output);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private static String readSafe(Path source) throws ApplicationException {
        try {
            return Files.readString(source);
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    public void run() throws ApplicationException {
        final var sources = collectSources();
        for (Path source : sources) {
            runWithSource(source);
        }
    }

    private Set<Path> collectSources() throws ApplicationException {
        try {
            return sourceSet.collect();
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private void runWithSource(Path source) throws ApplicationException {
        final var input = readSafe(source);
        final var output = new Compiler(input).compile();

        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf('.');
        if (separator == -1) return;

        final var applicationTest = fileName.substring(0, separator);
        final var targetName = applicationTest + EXTENSION_SEPARATOR + MAGMA_EXTENSION;
        final var target = source.resolveSibling(targetName);
        writeSafe(target, output);
    }
}