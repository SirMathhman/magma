package magma;

import java.util.Optional;

public class StringRule implements Rule {
    @Override
    public Optional<String> parse(String input) {
        return Optional.of(input);
    }
}