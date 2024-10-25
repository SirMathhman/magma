package magma;

import magma.app.Application;
import magma.app.DirectorySourceSet;
import magma.app.compile.JavaToMagmaPasser;
import magma.app.compile.MagmaToCParser;
import magma.app.compile.Passer;
import magma.app.compile.lang.CLang;
import magma.app.compile.lang.MagmaLang;
import magma.app.compile.rule.Rule;

import java.nio.file.Path;
import java.nio.file.Paths;

import static magma.app.Application.JAVA_EXTENSION;
import static magma.app.Application.MAGMA_EXTENSION;
import static magma.app.compile.lang.JavaLang.createRootRule;

public class Main {
    public static final Path ROOT = Paths.get(".", "src", "java");

    public static void main(String[] args) {
        run(JAVA_EXTENSION, createRootRule(),
                new JavaToMagmaPasser(),
                MAGMA_EXTENSION, MagmaLang.createRootRule());

        run(MAGMA_EXTENSION, MagmaLang.createRootRule(),
                new MagmaToCParser(),
                "c", CLang.createRootRule());
    }

    private static void run(String sourceExtension, Rule sourceRule, Passer passer, String targetExtension, Rule targetRule) {
        final var sourceSet = new DirectorySourceSet(Main.ROOT, "." + sourceExtension);
        new Application(sourceSet, sourceRule, passer, targetRule, sourceExtension, targetExtension)
                .run()
                .ifPresent(Throwable::printStackTrace);
    }
}
