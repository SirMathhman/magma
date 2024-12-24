package magma;

import java.util.Arrays;
import java.util.Optional;

public record StringListRule(String propertyKey, String delimiter) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        final var namespace = Arrays.stream(input.split(getDelimiter())).toList();
        return Optional.of(new Node().withStringList(propertyKey(), namespace));
    }

    @Override
    public Optional<String> generate(Node node) {
        final var namespace = node.findStringList(propertyKey());
        if (namespace.isEmpty()) return Optional.empty();

        final var namespaceString = String.join(delimiter(), namespace.get());
        return Optional.of(namespaceString);
    }

    public String getDelimiter() {
        return delimiter;
    }
}