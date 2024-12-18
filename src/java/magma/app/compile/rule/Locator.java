package magma.app.compile.rule;

import magma.api.option.Option;

public interface Locator {
    Option<Integer> locate(Input input, String slice);
}
