package magma.app;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.CompileResult;
import magma.app.compile.Compiler;
import magma.app.compile.pass.PassingStage;
import magma.app.compile.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public final class Application {
    public static final String MAGMA_EXTENSION = "mgs";
    public static final String EXTENSION_SEPARATOR = ".";
    public static final char FILE_NAME_SEPARATOR = '.';
    public static final String JAVA_EXTENSION = "java";
    private final String targetExtension;
    private final Rule sourceRule;
    private final SourceSet sourceSet;
    private final PassingStage passingStage;
    private final Rule targetRule;
    private final String sourceExtension;

    public Application(
            SourceSet sourceSet,
            Rule sourceRule,
            PassingStage passingStage,
            Rule targetRule,
            String sourceExtension,
            String targetExtension
    ) {
        this.sourceSet = sourceSet;
        this.sourceRule = sourceRule;
        this.passingStage = passingStage;
        this.targetRule = targetRule;

        this.sourceExtension = sourceExtension;
        this.targetExtension = targetExtension;
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

    private Optional<ApplicationException> writeResult(CompileResult result, Path source, String name) {
        return writeDebug(result.output(), source, name, targetExtension)
                .or(() -> writeDebug(result.beforePass().toString(), source, name, sourceExtension + ".in.ast"))
                .or(() -> writeDebug(result.afterPass().toString(), source, name, targetExtension + ".out.ast"));
    }

    private Optional<ApplicationException> compileWithInput(Path source, String input) {
        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf(FILE_NAME_SEPARATOR);
        if (separator == -1) return Optional.empty();

        final var fileNameWithoutExtension = fileName.substring(0, separator);
        return compileWithFileName(source, fileNameWithoutExtension, input);
    }

    private Optional<ApplicationException> compileWithFileName(Path source, String fileNameWithoutExtension, String input) {
        return new Compiler(input, sourceRule, passingStage, targetRule)
                .compile()
                .mapErr(err -> new ApplicationException(source.toAbsolutePath().toString(), err))
                .mapValue(result -> writeResult(result, source, fileNameWithoutExtension))
                .match(value -> value, Optional::of);
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
        System.out.println("Compiling: " + source);
        return Application.readSafe(source)
                .mapValue(input -> compileWithInput(source, input))
                .match(value -> value, Optional::of);
    }
}