package magma.app.compile.rule;

import magma.api.option.Option;

public interface Locator {
    Option<Integer> locate(String input);
}
