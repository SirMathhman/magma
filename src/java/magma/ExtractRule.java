package magma;

import java.util.Optional;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return Optional.of(new MapNode().withString(propertyKey, input));
    }

    @Override
    public Optional<String> generate(Node node) {
        return Optional.of(node.findString(propertyKey).orElse(""));
    }
}
