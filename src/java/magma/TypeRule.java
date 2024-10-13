package magma;

import java.util.Optional;

public record TypeRule(String type, Rule rule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return rule.parse(input).map(node -> node.retype(type));
    }

    @Override
    public Optional<String> generate(Node node) {
        if (!node.is(type)) return Optional.empty();
        return rule.generate(node);
    }
}
