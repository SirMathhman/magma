package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            final var workingDirectory = Paths.get(".", "src", "java", "magma");
            final var source = workingDirectory.resolve("Main.mgs");
            final var input = Files.readString(source);
            final var output = workingDirectory.resolve("Main.c");
            Files.writeString(output, compile(input));

            new ProcessBuilder("clang", "Main.c", "-o", "Magma.exe")
                    .directory(workingDirectory.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start()
                    .waitFor();
        } catch (IOException | InterruptedException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String root) throws CompileException {
        throw new CompileException("Invalid root", root);
    }
}
