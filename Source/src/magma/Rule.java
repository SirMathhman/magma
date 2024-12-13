package magma;

import java.util.Optional;

public interface Rule {
    Optional<String> generate(Node node);

    Optional<Node> parse(String input);
}
