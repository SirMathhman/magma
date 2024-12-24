package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Optional<String> generate(Node node) {
        return childRule().generate(node).map(value -> prefix() + value);
    }
}