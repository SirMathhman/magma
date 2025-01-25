package magma.app.compile.rule.locate;

import magma.api.stream.Stream;
import magma.api.stream.Streams;

public record FirstLocator(String infix) implements Locator {
    @Override
    public String unwrap() {
        return this.infix;
    }

    @Override
    public int length() {
        return this.infix.length();
    }

    @Override
    public Stream<Integer> locate(String input) {
        final var index = input.indexOf(this.infix);
        return index == -1 ? Streams.empty() : Streams.of(index);
    }
}