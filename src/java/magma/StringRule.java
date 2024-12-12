package magma;

import java.util.Optional;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Optional<String> generate(Node mapNode) {
        return mapNode.find(propertyKey());
    }
}