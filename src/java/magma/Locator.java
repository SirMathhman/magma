package magma;

import magma.option.Option;

public interface Locator {
    Option<Integer> locate(String input);

    int sliceLength();

    String infix();
}
