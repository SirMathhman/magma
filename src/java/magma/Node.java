package magma;

import java.util.List;
import java.util.Optional;

public record Node(List<String> namespace) {
    public Optional<List<String>> findNamespace() {
        return Optional.of(namespace);
    }
}