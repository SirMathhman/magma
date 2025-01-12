package magma.java;

import java.nio.file.Paths;

public class JavaPaths {
    public static magma.io.Path get(String first, String... more) {
        return new JavaPath(Paths.get(first, more));
    }
}
