package magma;

import java.util.Optional;

public class ClassCompiler implements Compiler {
    @Override
    public Optional<String> compile(String rootSegment) {
        return rootSegment.contains("class ")
                ? Optional.of("struct Temp {};")
                : Optional.empty();
    }
}