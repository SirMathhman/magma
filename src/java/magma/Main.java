package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            compileRoot(input);
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileRoot(String root) throws CompileException {
        throw new CompileException("Unknown root", root);
    }
}
