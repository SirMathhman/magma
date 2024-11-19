package magma;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Optional<String> parse(String segment) {
        if (!segment.startsWith(prefix())) return Optional.empty();
        final var substring = segment.substring(prefix().length());
        return childRule().parse(substring);
    }
}