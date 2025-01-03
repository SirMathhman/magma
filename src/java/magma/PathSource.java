package magma;

import java.nio.file.Path;

public record PathSource(Path source) implements Source {
    @Override
    public String computeName() {
        final var fullName = source().getFileName().toString();
        final var separator = fullName.indexOf('.');
        if (separator == -1) return fullName;
        return fullName.substring(0, separator);
    }
}