package magma;

import java.util.Optional;

public class StringRule implements Rule {
    private Optional<String> parse0(String input) {
        return Optional.of(input);
    }

    @Override
    public Optional<Node> parse(String input) {
        return parse0(input).map(value -> new Node(Optional.empty(), value));
    }
}