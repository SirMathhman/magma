package magma.compile.rule;

import magma.compile.MapNode;
import magma.compile.Node;
import magma.option.Option;
import magma.option.Some;

import java.util.Map;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Option<Node> parse(String input) {
        return new Some<>(new MapNode(Map.of(propertyKey(), input)));
    }

    @Override
    public Option<String> generate(Node node) {
        return node.findString(propertyKey);
    }
}