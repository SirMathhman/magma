package magma.compile.rule.locate;

import java.util.stream.Stream;

public class LastLocator implements Locator {
    @Override
    public Stream<Integer> locate(String input, String infix) {
        final var index = input.lastIndexOf(infix);
        return index == -1 ? Stream.empty() : Stream.of(index);
    }
}