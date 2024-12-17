package magma.app.compile.rule;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

public class LastLocator implements Locator {
    public static Locator Last = new LastLocator();

    private LastLocator() {
    }

    @Override
    public Option<Integer> locate(String value, String slice) {
        final var index = value.lastIndexOf(slice);
        return index == -1 ? new None<Integer>() : new Some<Integer>(index);
    }
}