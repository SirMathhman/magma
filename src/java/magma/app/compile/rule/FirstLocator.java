package magma.app.compile.rule;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public class FirstLocator implements Locator {
    public static final FirstLocator First = new FirstLocator();

    private FirstLocator() {
    }

    @Override
    public Option<Integer> locate(String value, String slice) {
        final var index = value.indexOf(slice);
        return index == -1 ? new None<Integer>() : new Some<Integer>(index);
    }
}