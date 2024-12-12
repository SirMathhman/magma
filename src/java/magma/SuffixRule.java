package magma;

import java.util.Optional;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Optional<String> generate(Node node) {
        return childRule()
                .generate(node)
                .map(value -> value + suffix());
    }
}