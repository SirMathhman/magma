package magma;

import java.util.Optional;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Optional<String> generate(Node node) {
        return childRule().generate(node).map(value -> value + suffix());
    }

    @Override
    public Optional<Node> parse(String input) {
        if (input.endsWith(suffix)) {
            return childRule.parse(input.substring(0, input.length() - suffix.length()));
        } else {
            return Optional.empty();
        }
    }
}