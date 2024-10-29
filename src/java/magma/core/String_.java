package magma.core;

import magma.core.option.Option;

public interface String_ {
    String_ concat(String slice);

    Option<Integer> firstIndexOfChar(char c);

    String unwrap();

    Option<String_> substring(int start, int end);
}
