package magma.java;

import magma.api.io.Path;

import java.nio.file.Paths;

public class JavaPaths {
    public static Path get(String first, String... more) {
        return new JavaPath(Paths.get(first, more));
    }
}
