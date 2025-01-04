package magma;

import java.util.Optional;

public interface Compiler {
    Optional<String> compile(String rootSegment);
}
