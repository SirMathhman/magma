package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Define paths
            final Path source = Paths.get(".", "working", "main.mgs");
            final Path target = Paths.get(".", "working", "main.c");

            // Read from source file and write to target file
            final String input = Files.readString(source);
            Files.writeString(target, input);

            // Compile the program
            final int compileExitCode = execute(List.of(
                    "clang", "main.c", "-o", "magma.exe"
            ));

            // Check if the compilation was successful
            if (compileExitCode != 0) {
                System.err.println("Compilation failed with exit code: " + compileExitCode);
                return;
            }

            // Run the compiled executable using cmd.exe
            final int runExitCode = execute(List.of("cmd.exe", "/c", "magma"));
            if (runExitCode != 0) {
                System.err.println("Program execution failed with exit code: " + runExitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static int execute(List<String> command) throws InterruptedException, IOException {
        // Construct the ProcessBuilder with separate arguments
        var processBuilder = new ProcessBuilder(command);

        // Set working directory
        processBuilder.directory(Paths.get(".", "working").toFile());

        // Redirect output and error streams
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

        // Start the process and wait for it to finish
        return processBuilder.start().waitFor();
    }
}
