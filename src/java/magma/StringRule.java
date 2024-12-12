package magma;

import java.util.Optional;

public record StringRule(String propertyKey) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return Optional.of(new MapNode().withString(propertyKey(), input));
    }

    @Override
    public Optional<String> generate(Node mapNode) {
        return mapNode.find(propertyKey());
    }
}