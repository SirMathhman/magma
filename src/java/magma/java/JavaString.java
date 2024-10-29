package magma.java;

import magma.core.String_;
import magma.core.option.None;
import magma.core.option.Option;
import magma.core.option.Some;

public final class JavaString implements String_ {
    public static final String_ EMPTY = new JavaString("");
    private final String value;

    public JavaString(String value) {
        this.value = value;
    }

    @Override
    public boolean equalsTo(String_ other) {
        return value.equals(other.unwrap());
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

    @Override
    public Option<String_> substring(int start, int end) {
        final var length = value.length();
        if (start >= 0 && end >= 0 && end < length && start <= end) {
            return new Some<>(new JavaString(value.substring(start, end)));
        } else {
            return new None<>();
        }
    }

    @Override
    public String_ concat(String slice) {
        return new JavaString(unwrap() + slice);
    }
}