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
    public Option<String_> slice(int start, int end) {
        final var length = value.length();
        if (start >= 0 && end >= 0 && start <= end && end <= length) {
            return new Some<>(new JavaString(value.substring(start, end)));
        } else {
            return new None<>();
        }
    }

    @Override
    public String_ prependSlice(String slice) {
        return new JavaString(slice + value);
    }

    @Override
    public boolean startsWithSlice(String slice) {
        return value.startsWith(slice);
    }

    @Override
    public boolean endsWithSlice(String slice) {
        return value.endsWith(slice);
    }

    @Override
    public Option<String_> truncateLeftBySlice(String slice) {
        return startsWithSlice(slice)
                ? slice(slice.length(), value.length())
                : new None<>();
    }

    @Override
    public Option<String_> truncateRightBySlice(String slice) {
        return endsWithSlice(slice)
                ? slice(0, value.length() - slice.length())
                : new None<>();
    }

    @Override
    public String_ appendOwned(String_ other) {
        return new JavaString(value + other.unwrap());
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public String_ appendSlice(String slice) {
        return new JavaString(unwrap() + slice);
    }
}