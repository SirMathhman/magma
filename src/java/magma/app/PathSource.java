package magma.app;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record PathSource(Path root, Path source) {
    public static final char EXTENSION_SEPARATOR = '.';

    Result<String, ApplicationException> read() {
        try {
            return new Ok<>(Files.readString(source()));
        } catch (IOException e) {
            return new Err<>(new ApplicationException(e));
        }
    }

    public String computeName() {
        final var fileName = source.getFileName().toString();
        final var separator = fileName.indexOf(EXTENSION_SEPARATOR);
        return separator == -1 ? fileName : fileName.substring(0, separator);
    }

    public List<String> computeNamespace() {
        var segments = new ArrayList<String>();
        final var relativized = root.relativize(source);

        int i = 0;
        while (i < relativized.getNameCount() - 1) {
            segments.add(relativized.getName(i).toString());
            i++;
        }

        return segments;
    }
}