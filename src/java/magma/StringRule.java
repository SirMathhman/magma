package magma;

import java.util.Optional;

public class StringRule implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        return Optional.of(new Node().withString(Compiler.VALUE, input));
    }

    @Override
    public Optional<String> generate(Node node) {
        return Optional.of(node.findValue(Compiler.VALUE).orElseThrow());
    }
}