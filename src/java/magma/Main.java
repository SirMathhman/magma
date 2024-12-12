package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "working", "main.mgs");
            final var input = Files.readString(source);
            Files.writeString(source.resolveSibling("main.casm"), input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
