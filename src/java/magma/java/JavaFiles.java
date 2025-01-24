package magma.java;

import magma.api.io.Path;

import java.nio.file.Files;

public class JavaFiles {
    public static boolean isRegularFile(Path path) {
        return Files.isRegularFile(path.unwrap());
    }
}
