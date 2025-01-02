package magma.io;

import magma.api.JavaFiles;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Path;

public record Source(Path root, Path child) {
    public Result<String, IOException> read() {
        return JavaFiles.readString(child());
    }

    public Path resolve(Path root, String extension) {
        final var relativized = this.root.relativize(child);
        final var parent = relativized.getParent();
        final var name = relativized.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        return root.resolve(parent).resolve(nameWithoutExt + extension);
    }
}
