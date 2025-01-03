package magma.app.io.unit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record PathUnit(Path root, Path child) implements Unit {
    @Override
    public String computeName() {
        final var fullName = child.getFileName().toString();
        final var separator = fullName.indexOf('.');
        if (separator == -1) return fullName;
        return fullName.substring(0, separator);
    }

    @Override
    public List<String> computeNamespace() {
        final var relativized = root.relativize(child);
        var segments = new ArrayList<String>();
        for (Path path : relativized.getParent()) {
            segments.add(path.toString());
        }
        return segments;
    }

    @Override
    public String read() throws IOException {
        return Files.readString(child);
    }
}