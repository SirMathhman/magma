package magma.rule;

import magma.MapNode;
import magma.Node;

import java.util.Optional;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        Node node = new MapNode();
        return Optional.of(node.strings().with(propertyKey, input));
    }

    @Override
    public Optional<String> generate(Node node) {
        return Optional.of(node.strings().find(propertyKey).orElse(""));
    }
}
