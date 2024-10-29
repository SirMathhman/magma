package magma.java;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record JavaString(String fileName) {
    Option<Integer> firstIndexOfChar(char c) {
        final var index = fileName().indexOf(c);
        if (index == -1) return new None<>();
        return new Some<>(index);
    }
}