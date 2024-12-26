package magma.compile.rule.split.locate;

import java.util.stream.Stream;

public class FirstLocator implements Locator {
    @Override
    public Stream<Integer> locate(String input, String infix) {
        final var index = input.indexOf(infix);
        return index == -1 ? Stream.empty() : Stream.of(index);
    }
}