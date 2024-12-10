package magma;

import java.util.Optional;

public record TypeRule(String type, Rule childRule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return childRule.parse(input).map(node -> node.retype(type));
    }
}
