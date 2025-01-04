package magma;

import java.util.Optional;

public class ImportCompiler implements Compiler {
    @Override
    public Optional<String> compile(String rootSegment) {
        if (!rootSegment.startsWith("import ") || !rootSegment.endsWith(";")) return Optional.empty();

        final var slice = rootSegment.substring("import ".length(), rootSegment.length() - 1);
        final var args = slice.split("\\.");
        final var joined = String.join("/", args);
        return Optional.of("#include \"" + joined + ".h\"\n");
    }
}