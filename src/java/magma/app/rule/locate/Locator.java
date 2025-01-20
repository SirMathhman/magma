package magma.app.rule.locate;

import java.util.Optional;

public interface Locator {
    String unwrap();

    int length();

    Optional<Integer> locate(String input);
}
