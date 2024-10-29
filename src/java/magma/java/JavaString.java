package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record JavaString(String value) {
    Option<Integer> firstIndexOfChar(char c) {
        final var index = value().indexOf(c);
        if (index == -1) return new None<>();
        return new Some<>(index);
    }
}