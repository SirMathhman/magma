package magma.rule;

import magma.Node;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return rules.stream()
                .map(rule -> rule.parse(input))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<String> generate(Node node) {
        return rules.stream()
                .map(rule -> rule.generate(node))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
