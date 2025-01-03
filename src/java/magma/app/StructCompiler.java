package magma.app;

import java.util.Optional;

public record StructCompiler(String infix) implements Compiler {
    @Override
    public Optional<String> compile(String input) {
        if (input.contains(infix())) return Optional.of("struct Temp {};");
        return Optional.empty();
    }
}