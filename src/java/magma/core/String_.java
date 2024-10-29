package magma.core;

import magma.core.option.Option;

public interface String_ {
    String_ appendSlice(String slice);

    Option<Integer> firstIndexOfChar(char c);

    String unwrap();

    Option<String_> slice(int start, int end);

    String_ prependSlice(String slice);

    boolean startsWithSlice(String slice);

    boolean endsWithSlice(String slice);

    Option<String_> truncateLeftBySlice(String slice);

    Option<String_> truncateRightBySlice(String slice);
}
