package magma;

import java.util.Map;

public record ExtractRule(String propertyKey) implements Rule {
    @Override
    public Option<MapNode> parse(String input) {
        return new Some<>(new MapNode(Map.of(propertyKey(), input)));
    }

    @Override
    public Option<String> generate(MapNode node) {
        return node.findString(propertyKey);
    }
}