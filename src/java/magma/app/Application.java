package magma.app;

import magma.api.result.Results;
import magma.app.compile.Compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
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

    private static void writeDebug(Path source, String output, String name, String extension) throws ApplicationException {
        final var targetName = name + EXTENSION_SEPARATOR + extension;
        final var target = source.resolveSibling(targetName);
        writeSafe(target, output);
    }

    public void run() throws ApplicationException {
        final var sources = collectSources();
        Iterator<Path> iterator = sources.iterator();
        while (iterator.hasNext()) {
            Path source = iterator.next();
            runWithSource(source);
        }
    }

    private Set<Path> collectSources() throws ApplicationException {
        try {
            return Results.unwrap(sourceSet.collect());
        } catch (IOException e) {
            throw new ApplicationException(e);
        }
    }

    private void runWithSource(Path source) throws ApplicationException {
        final var input = readSafe(source);

        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf('.');
        if (separator == -1) return;
        final var fileNameWithoutExtension = fileName.substring(0, separator);

        var compiler = new Compiler(input);
        final var result = Results.unwrap(compiler.compile());

        writeDebug(source, result.output(), fileNameWithoutExtension, MAGMA_EXTENSION);
        writeDebug(source, result.beforePass().toString(), fileNameWithoutExtension, "in.ast");
        writeDebug(source, result.afterPass().toString(), fileNameWithoutExtension, "out.ast");
    }
}