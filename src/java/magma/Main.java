package magma;

import magma.app.Application;
import magma.app.compile.CompileException;
import magma.app.io.source.DirectorySourceSet;
import magma.app.io.target.DirectoryTargetSet;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var sourceSet = new DirectorySourceSet("java", Paths.get(".", "src", "java"));
            final var targetSet = new DirectoryTargetSet(Paths.get(".", "src", "c"));
            new Application(sourceSet, targetSet).run();
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
