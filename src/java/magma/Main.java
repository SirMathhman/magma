package magma;

import magma.app.Application;
import magma.app.DirectorySourceSet;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.pass.PassingStage;
import magma.app.compile.pass.SequentialPassingStage;
import magma.app.compile.pass.TreePassingStage;
import magma.app.compile.rule.Rule;
import magma.java.JavaList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static magma.app.Application.JAVA_EXTENSION;
import static magma.app.Application.MAGMA_EXTENSION;
import static magma.app.compile.lang.JavaLang.createRootRule;

public class Main {
    public static final PassingStage JAVA_MAGMA_PASS = new SequentialPassingStage(new JavaList<PassingStage>().add(new TreePassingStage(Collections.emptyList())));
    public static final SequentialPassingStage MAGMA_C_PASS = new SequentialPassingStage(new JavaList<PassingStage>().add(new TreePassingStage(Collections.emptyList())));

    public static void main(String[] args) {
        run(Paths.get(".", "src", "java"), createRootRule(), JAVA_EXTENSION,
                JAVA_MAGMA_PASS,
                Paths.get(".", "build"), MagmaLang.createRootRule(), MAGMA_EXTENSION);
    }

    private static void run(Path sourceRoot, Rule sourceRule, String sourceExtension, PassingStage passingStage, Path targetRoot, Rule targetRule, String targetExtension) {
        final var sourceSet = new DirectorySourceSet(sourceRoot, "." + sourceExtension);
        new Application(sourceSet, sourceExtension, sourceRule, passingStage, targetExtension, targetRule, targetRoot)
                .run()
                .ifPresent(Throwable::printStackTrace);
    }
}
