package magma.app.compile.rule;

import java.util.Optional;

public interface Locator {
    Optional<Integer> locate(String input);

    String slice();
}
