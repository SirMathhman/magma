package magma;

import java.util.Optional;

public interface Locator {
    Optional<Integer> locate(String input);

    int length();
}
