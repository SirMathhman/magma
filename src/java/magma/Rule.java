package magma;

import java.util.Optional;

public interface Rule {
    Optional<String> generate(Node mapNode);
}
