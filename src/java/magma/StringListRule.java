package magma;

import java.util.Optional;

public record StringListRule(String propertyKey, String delimiter) implements Rule {
    @Override
    public Optional<String> generate(Node node) {
        final var namespace = node.findStringList(propertyKey());
        if (namespace.isEmpty()) return Optional.empty();

        final var namespaceString = String.join(delimiter(), namespace.get());
        return Optional.of(namespaceString);
    }
}