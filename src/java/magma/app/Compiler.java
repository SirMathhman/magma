package magma.app;

import java.util.Optional;

public interface Compiler {
    Optional<String> compile(String input);
}
