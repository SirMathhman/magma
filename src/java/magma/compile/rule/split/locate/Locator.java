package magma.compile.rule.split.locate;

import java.util.stream.Stream;

public interface Locator {
    Stream<Integer> locate(String input, String infix);
}
