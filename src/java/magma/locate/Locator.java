package magma.locate;

import java.util.Optional;

public interface Locator {
    int computeLength();

    Optional<Integer> locate(String input);
}
