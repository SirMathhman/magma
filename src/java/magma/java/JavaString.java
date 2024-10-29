package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public final class JavaString implements String_ {
    private final String value;

    public JavaString(String value) {
        this.value = value;
    }

    @Override
    public Option<Integer> firstIndexOfChar(char c) {
        final var index = value.indexOf(c);
        if (index == -1) return new None<>();
        return new Some<>(index);
    }

    @Override
    public String unwrap() {
        return value;
    }
}