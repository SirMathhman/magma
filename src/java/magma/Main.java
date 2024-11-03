package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        final var source = Paths.get(".", "src", "magma", "main.mgs");
        final var target = source.resolveSibling("main.c");
        writeSafe(target, "int main(){\n\treturn 0;\n}").ifPresent(Throwable::printStackTrace);
    }

    private static Option<IOException> writeSafe(Path target, String output) {
        try {
            Files.writeString(target, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(e);
        }
    }
}
