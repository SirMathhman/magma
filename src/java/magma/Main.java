package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "magma", "main.mgs");
            Files.writeString(source.resolveSibling("main.c"), Files.readString(source));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
