package magma.app;

import magma.api.result.Result;
import magma.app.compile.CompileResult;
import magma.app.compile.Compiler;
import magma.app.compile.pass.PassingStage;
import magma.app.compile.rule.Rule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public final class Application {
    public static final String JAVA_EXTENSION = "java";
    public static final String MAGMA_EXTENSION = "mgs";
    public static final String C_EXTENSION = "c";

    public static final String EXTENSION_SEPARATOR = ".";
    public static final String DEBUG_SUFFIX = "-debug";
    private final Path targetRoot;
    private final String targetExtension;
    private final Rule sourceRule;
    private final SourceSet sourceSet;
    private final PassingStage passingStage;
    private final Rule targetRule;
    private final String sourceExtension;

    public Application(
            SourceSet sourceSet,
            String sourceExtension, Rule sourceRule,
            PassingStage passingStage,
            String targetExtension, Rule targetRule,
            Path targetRoot
    ) {
        this.sourceSet = sourceSet;
        this.sourceRule = sourceRule;
        this.passingStage = passingStage;
        this.targetRule = targetRule;

        this.sourceExtension = sourceExtension;
        this.targetExtension = targetExtension;
        this.targetRoot = targetRoot;
    }

    private static Optional<ApplicationException> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(new ApplicationException(e));
        }
    }

    private Optional<ApplicationException> write(
            PathSource pathSource,
            String targetDirectory,
            String targetExtension,
            String targetOutput
    ) {
        final var targetName = pathSource.computeName() + EXTENSION_SEPARATOR + targetExtension;
        final var namespace = pathSource.computeNamespace();

        var parent = targetRoot.resolve(targetDirectory);
        int i = 0;
        while (i < namespace.size()) {
            String segment = namespace.get(i);
            parent = parent.resolve(segment);
            i++;
        }

        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                return Optional.of(new ApplicationException(e));
            }
        }

        final var target = parent.resolve(targetName);
        return writeSafe(target, targetOutput);
    }

    private Optional<ApplicationException> compileWithInput(PathSource source, String input) {
        return compileWithFileName(input, source);
    }

    private Optional<ApplicationException> compileWithFileName(String input, PathSource source) {
        return new Compiler(input, sourceRule, passingStage, targetRule)
                .compile()
                .mapErr(err -> new ApplicationException(source.toString(), err))
                .mapValue(result -> writeResult(source, result))
                .match(value -> value, Optional::of);
    }

    private Optional<ApplicationException> writeResult(PathSource source, CompileResult result) {
        return write(source, targetExtension, targetExtension, result.output())
                .or(() -> write(source, sourceExtension + DEBUG_SUFFIX, sourceExtension + ".in.ast", result.beforePass().toString()))
                .or(() -> write(source, targetExtension + DEBUG_SUFFIX, targetExtension + ".out.ast", result.afterPass().toString()));
    }

    public Optional<ApplicationException> run() {
        return collectSources().match(sources -> {
            var iterator = sources.iterator();
            while (iterator.hasNext()) {
                var source = iterator.next();
                final var error = runWithSource(source);
                if (error.isPresent()) {
                    return error;
                }
            }

            return Optional.empty();
        }, Optional::of);
    }

    private Result<Set<PathSource>, ApplicationException> collectSources() {
        return sourceSet.collect().mapErr(ApplicationException::new);
    }

    private Optional<ApplicationException> runWithSource(PathSource source) {
        System.out.println("Compiling: " + source);
        return source.read()
                .mapValue(input -> compileWithInput(source, input))
                .match(value -> value, Optional::of);
    }
}