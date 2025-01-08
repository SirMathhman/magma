package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            // Define paths
            final Path source = Paths.get(".", "working", "main.mgs");
            final Path target = Paths.get(".", "working", "main.c");

            // Read from source file and write to target file
            final String input = Files.readString(source);
            Files.writeString(target, input);

            // Construct the ProcessBuilder with separate arguments
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "clang", "main.c", "-o", "magma.exe"
            );

            // Set working directory
            processBuilder.directory(Paths.get(".", "working").toFile());

            // Start the process and wait for it to finish
            int exitCode = processBuilder.start().waitFor();

            // Check if the process was successful
            if (exitCode == 0) {
                System.out.println("Compilation successful!");
            } else {
                System.out.println("Compilation failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}