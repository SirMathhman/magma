package magma.compile.rule;

import magma.compile.MapNode;
import magma.compile.Node;
import magma.core.String_;
import magma.core.option.Option;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Option<Node> parse(String_ input) {
        return new MapNode().withString(propertyKey(), input);
    }

    @Override
    public Option<String_> generate(Node node) {
        return node.find(propertyKey);
    }
}