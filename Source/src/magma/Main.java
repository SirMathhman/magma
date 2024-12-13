package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "Source", "src", "magma", "Main.java");
            final var input = Files.readString(source);

            final var targetDirectory = Paths.get(".", "CompiledTarget", "src", "magma");
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }

            final var target = targetDirectory.resolve("Main.mgs");
            Files.writeString(target, input);

            final var source0 = Files.readString(target);
            final var otherTarget = Paths.get(".", "NativeTarget", "src", "magma");
            if (!Files.exists(otherTarget)) {
                Files.createDirectories(otherTarget);
            }

            Files.writeString(otherTarget.resolve("Main.java"), source0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
