package magma;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    @Override
    public Optional<String> parse(String input) {
        return rules.stream()
                .map(rule -> rule.parse(input))
                .flatMap(Optional::stream)
                .findFirst();
    }
}
