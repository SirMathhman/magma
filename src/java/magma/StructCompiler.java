package magma;

import java.util.Optional;

public class StructCompiler implements Compiler {

    private final String infix;

    public StructCompiler(String infix) {
        this.infix = infix;
    }

    @Override
    public Optional<String> compile(String rootSegment) {
        return rootSegment.contains(infix)
                ? Optional.of("struct Temp {};")
                : Optional.empty();
    }
}