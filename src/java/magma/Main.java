package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        final var source = Paths.get(".", "src", "magma", "main.mgs");
        final var target = source.resolveSibling("main.c");
        writeSafe(target).ifPresent(Throwable::printStackTrace);
    }

    private static Option<IOException> writeSafe(Path target) {
        try {
            Files.writeString(target, "");
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }
}
