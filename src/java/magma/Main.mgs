package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            Files.writeString(source.resolveSibling("Main.mgs"), input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
