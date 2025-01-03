package magma.app;

import java.util.Arrays;
import java.util.Optional;

public class ImportCompiler implements Compiler {
    @Override
    public Optional<String> compile(String rootSegment) {
        if (!rootSegment.startsWith("import ")) return Optional.empty();

        final var right = rootSegment.substring("import ".length());
        if (!right.endsWith(";")) return Optional.empty();

        final var center = right.substring(0, right.length() - ";".length());
        final var segments = Arrays.stream(center.split("\\.")).toList();
        return Optional.of("#include \"" + String.join("/", segments) + ".h\"\n");
    }
}