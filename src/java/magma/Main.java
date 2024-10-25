package magma;

import magma.app.Application;
import magma.app.DirectorySourceSet;
import magma.app.compile.lang.CLang;
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
    public static final Path ROOT = Paths.get(".", "src", "java");
    public static final PassingStage JAVA_MAGMA_PASS = new SequentialPassingStage(new JavaList<PassingStage>().add(new TreePassingStage(Collections.emptyList())));
    public static final SequentialPassingStage MAGMA_C_PASS = new SequentialPassingStage(new JavaList<PassingStage>().add(new TreePassingStage(Collections.emptyList())));

    public static void main(String[] args) {
        run(JAVA_EXTENSION, createRootRule(),
                JAVA_MAGMA_PASS,
                MAGMA_EXTENSION, MagmaLang.createRootRule());

        run(MAGMA_EXTENSION, MagmaLang.createRootRule(),
                MAGMA_C_PASS,
                "c", CLang.createRootRule());
    }

    private static void run(String sourceExtension, Rule sourceRule, PassingStage passingStage, String targetExtension, Rule targetRule) {
        final var sourceSet = new DirectorySourceSet(Main.ROOT, "." + sourceExtension);
        new Application(sourceSet, sourceRule, passingStage, targetRule, sourceExtension, targetExtension)
                .run()
                .ifPresent(Throwable::printStackTrace);
    }
}
