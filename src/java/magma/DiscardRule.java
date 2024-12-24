package magma;

import java.util.Optional;

public class DiscardRule implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return Optional.of(new Node());
    }

    @Override
    public Optional<String> generate(Node node) {
        return Optional.of("");
    }
}
