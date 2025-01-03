package magma.app.io.target;

import magma.app.io.unit.Unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record DirectoryTargetSet(Path root) implements TargetSet {
    @Override
    public void write(Unit unit, String output) throws IOException {
        final var namespace = unit.computeNamespace();
        final var name = unit.computeName();
        final var parent = resolveParent(namespace);
        if (!Files.exists(parent)) Files.createDirectories(parent);
        Files.writeString(parent.resolve(name + ".c"), output);
    }

    private Path resolveParent(List<String> namespace) {
        var current = root;
        for (var segment : namespace) {
            current = current.resolve(segment);
        }
        return current;
    }
}