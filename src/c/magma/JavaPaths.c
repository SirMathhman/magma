import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
struct JavaPaths {
	Optional<IOException> createDirectoriesSafe(Path targetParent){try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        }catch (IOException e) {
            return Optional.of(e);
        }
	}
	Optional<IOException> writeSafe(Path target, Path target String output){try {
            Files.writeString(target, output);
            return Optional.empty();
        }catch (IOException e) {
            return Optional.of(e);
        }
	}
	IOException> readSafe(Path source){try {
            return new Ok<>(Files.readString(source));
        }catch (IOException e) {
            return new Err<>(e);
        }
	}
}