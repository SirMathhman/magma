package magma;

import java.util.Optional;

public record ExactRule(String slice) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return input.equals(slice) ? Optional.of(new Node()) : Optional.empty();
    }

    @Override
    public Optional<String> generate(Node node) {
        return Optional.of(slice);
    }
}
