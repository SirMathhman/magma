package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "main.mgs");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("main.c");
            Files.writeString(target, compile(input));

            final var process = new ProcessBuilder("clang", "main.c", "-o", "main.exe")
                    .directory(Paths.get(".", "src", "java", "magma").toFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String input) {
        return input;
    }
}
