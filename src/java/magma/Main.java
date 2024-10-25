package magma;

import magma.app.Application;
import magma.app.DirectorySourceSet;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.pass.PassingStage;
import magma.app.compile.pass.SequentialPassingStage;
import magma.app.compile.pass.TreePassingStage;
import magma.app.compile.rule.Rule;
import magma.java.JavaList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static magma.app.Application.*;

public class Main {
    public static final PassingStage JAVA_MAGMA_PASS = new SequentialPassingStage(new JavaList<PassingStage>().add(new TreePassingStage(Collections.emptyList())));
    public static final SequentialPassingStage MAGMA_C_PASS = new SequentialPassingStage(new JavaList<PassingStage>().add(new TreePassingStage(Collections.emptyList())));
    public static final Path JAVA_SOURCE = Paths.get(".", "src", "java");
    public static final Path MAGMA_SOURCE = Paths.get(".", "build", "java-magma");
    public static final Path C_SOURCE = Paths.get(".", "build", "magma-c");

    public static void main(String[] args) {
        run(JAVA_SOURCE, JavaLang.createRootRule(), JAVA_EXTENSION,
                JAVA_MAGMA_PASS,
                MAGMA_SOURCE, MagmaLang.createRootRule(), MAGMA_EXTENSION);

        run(MAGMA_SOURCE, MagmaLang.createRootRule(), MAGMA_EXTENSION,
                MAGMA_C_PASS,
                C_SOURCE, CLang.createRootRule(), C_EXTENSION);
    }

    private static void run(Path sourceRoot, Rule sourceRule, String sourceExtension, PassingStage passingStage, Path targetRoot, Rule targetRule, String targetExtension) {
        final var sourceSet = new DirectorySourceSet(sourceRoot, "." + sourceExtension);
        new Application(sourceSet, sourceExtension, sourceRule, passingStage, targetExtension, targetRule, targetRoot)
                .run()
                .ifPresent(Throwable::printStackTrace);
    }
}
