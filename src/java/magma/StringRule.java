package magma;

import java.util.Optional;

public class StringRule implements Rule {
    private final String propertyKey;

    public StringRule(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    public Optional<String> generate(Node node) {
        return node.findString(propertyKey);
    }
}