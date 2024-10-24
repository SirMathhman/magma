package magma.app;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.CompileResult;
import magma.app.compile.Compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public final class Application {
    public static final String MAGMA_EXTENSION = "mgs";
    public static final String EXTENSION_SEPARATOR = ".";
    private final SourceSet sourceSet;

    public Application(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
    }

    private static Optional<ApplicationException> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(new ApplicationException(e));
        }
    }

    private static Result<String, ApplicationException> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(new ApplicationException(e));
        }
    }

    private static Optional<ApplicationException> writeDebug(String output, Path source, String name, String extension) {
        final var targetName = name + EXTENSION_SEPARATOR + extension;
        final var target = source.resolveSibling(targetName);
        return writeSafe(target, output);
    }

    private static Optional<ApplicationException> compileWithInput(Path source, String input) {
        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf('.');
        if (separator == -1) return Optional.empty();

        final var fileNameWithoutExtension = fileName.substring(0, separator);
        return new Compiler(input)
                .compile()
                .mapValue(result -> writeResult(result, source, fileNameWithoutExtension))
                .match(value -> value, Optional::of);
    }

    private static Optional<ApplicationException> writeResult(CompileResult result, Path source, String name) {
        return writeDebug(result.output(), source, name, MAGMA_EXTENSION)
                .or(() -> writeDebug(result.beforePass().toString(), source, name, "in.ast"))
                .or(() -> writeDebug(result.afterPass().toString(), source, name, "out.ast"));
    }

    public Optional<ApplicationException> run() {
        return collectSources().match(sources -> {
            Iterator<Path> iterator = sources.iterator();
            while (iterator.hasNext()) {
                Path source = iterator.next();
                final var error = runWithSource(source);
                if (error.isPresent()) {
                    return error;
                }
            }

            return Optional.empty();
        }, Optional::of);
    }

    private Result<Set<Path>, ApplicationException> collectSources() {
        return sourceSet.collect().mapErr(ApplicationException::new);
    }

    private Optional<ApplicationException> runWithSource(Path source) {
        return Application.readSafe(source)
                .mapValue(input -> compileWithInput(source, input))
                .match(value -> value, Optional::of);
    }
}