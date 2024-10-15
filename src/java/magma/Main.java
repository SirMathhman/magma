package magma;

import magma.app.Application;
import magma.app.ApplicationException;
import magma.app.DirectorySourceSet;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java");
            final var sourceSet = new DirectorySourceSet(source);
            new Application(sourceSet).run();
        } catch (ApplicationException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
