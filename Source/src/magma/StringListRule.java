package magma;

import java.util.Arrays;
import java.util.Optional;

public record StringListRule(String propertyKey, String delimiter) implements Rule {
    @Override
    public Optional<String> generate(Node node) {
        return node.findStringList(propertyKey).map(segments -> String.join(delimiter, segments));
    }

    @Override
    public Optional<Node> parse(String input) {
        return Optional.of(new MapNode().withStringList(propertyKey, Arrays.stream(input.split(delimiter)).toList()));
    }
}