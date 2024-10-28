package magma;

import magma.app.Application;
import magma.app.ApplicationException;
import magma.app.DirectorySourceSet;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.pass.*;
import magma.app.compile.pass.format.ImportFormatter;
import magma.app.compile.pass.trait.TraitAdapter;
import magma.app.compile.rule.Rule;
import magma.java.JavaList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static magma.app.Application.*;
import static magma.app.compile.lang.CommonLang.IMPORT_TYPE;
import static magma.app.compile.lang.MagmaLang.TRAIT_TYPE;

public class Main {
    public static final Path JAVA_SOURCE = Paths.get(".", "src", "java");
    public static final Path MAGMA_SOURCE = Paths.get(".", "src");
    public static final Path C_SOURCE = Paths.get(".", "build", "magma-c");

    private static SequentialPassingStage createMagmaCPasser() {
        return new SequentialPassingStage(new JavaList<PassingStage>()
                .add(new TreePassingStage(List.of(
                        new FilteredPasser(TRAIT_TYPE, new TraitAdapter())
                )))
                .add(createMagmaFormattingStage()));
    }

    private static SequentialPassingStage createJavaMagmaPasser() {
        return new SequentialPassingStage(new JavaList<PassingStage>()
                .add(getElement())
                .add(createMagmaFormattingStage()));
    }

    private static TreePassingStage createMagmaFormattingStage() {
        return new TreePassingStage(List.of(
                new FilteredPasser(IMPORT_TYPE, new ImportFormatter())
        ));
    }

    private static TreePassingStage getElement() {
        return new TreePassingStage(List.of(
                new PackageRemover(),
                new ImportAdapter(),
                new ClassAdapter(),
                new InterfaceAdapter(),
                new RecordAdapter()
        ));
    }

    public static void main(String[] args) {
        compileMagma().or(Main::compileC).ifPresent(Throwable::printStackTrace);
    }

    private static Optional<ApplicationException> compileC() {
        return run(MAGMA_SOURCE, MagmaLang.createRootRule(), MAGMA_EXTENSION,
                createMagmaCPasser(),
                C_SOURCE, MagmaLang.createRootRule(), C_EXTENSION);
    }

    private static Optional<ApplicationException> compileMagma() {
        return run(JAVA_SOURCE, JavaLang.createRootRule(), JAVA_EXTENSION,
                createJavaMagmaPasser(),
                MAGMA_SOURCE, MagmaLang.createRootRule(), MAGMA_EXTENSION);
    }

    private static Optional<ApplicationException> run(Path sourceRoot, Rule sourceRule, String sourceExtension, PassingStage passingStage, Path targetRoot, Rule targetRule, String targetExtension) {
        final var sourceSet = new DirectorySourceSet(sourceRoot, "." + sourceExtension);
        return new Application(sourceSet, sourceExtension, sourceRule, passingStage, targetExtension, targetRule, targetRoot).run();
    }
}
