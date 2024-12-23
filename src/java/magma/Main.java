package magma;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static final Path SOURCE = Paths.get(".", "src", "java", "magma", "Main.java");

    public static void main(String[] args) {
        try {
            Application.run(SOURCE);
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }
}
