package magma.app.compile.rule;

import java.util.stream.Stream;

public interface Locator {
    Stream<Integer> locate(String input);

    String slice();
}
