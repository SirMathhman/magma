package magma;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    private Optional<String> parse0(String input) {
        return rules.stream()
                .map(rule -> rule.parse(input).map(Node::value))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Node> parse(String input) {
        return parse0(input).map(value -> new Node(Optional.empty(), value));
    }
}
