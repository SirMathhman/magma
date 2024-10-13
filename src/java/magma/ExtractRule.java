package magma;

import java.util.Optional;

public class ExtractRule implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return Optional.of(new Node(input));
    }

    @Override
    public Optional<String> generate(Node node) {
        return Optional.of(node.name());
    }
}
