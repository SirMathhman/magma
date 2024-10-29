package magma.core;

import magma.core.option.Option;

public interface String_ {
    String_ appendSlice(String slice);

    boolean equalsTo(String_ other);

    Option<Integer> firstIndexOfChar(char c);

    String unwrap();

    Option<String_> substring(int start, int end);

    String_ prependSlice(String slice);
}
