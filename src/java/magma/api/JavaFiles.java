package magma.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class JavaFiles {
    public static Optional<IOException> writeString(Path path, String content) {
        try {
            Files.writeString(path, content);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }
}
