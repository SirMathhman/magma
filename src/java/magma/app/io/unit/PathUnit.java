package magma.app.io.unit;

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
        for (Path path : relativized) {
            segments.add(path.toString());
        }
        return segments;
    }
}