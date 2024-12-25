package magma.compile.rule.locate;

import java.util.stream.Stream;

public interface Locator {
    Stream<Integer> locate(String input, String infix);
}
