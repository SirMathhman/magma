package magma;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input);
            if (parsed.isPresent()) return parsed;
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> generate(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if (generated.isPresent()) return generated;
        }

        return Optional.empty();
    }
}
