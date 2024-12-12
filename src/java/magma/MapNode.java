package magma;

import java.util.Map;
import java.util.Optional;

public record MapNode(Map<String, String> node) implements Node {
    @Override
    public Optional<String> find(String propertyKey) {
        return Optional.ofNullable(node().get(propertyKey));
    }
}