package magma.app.compile.lang;

import magma.app.compile.rule.Locator;

import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;

public record ForwardsLocator(String delimiter) implements Locator {
    @Override
    public String slice() {
        return delimiter;
    }

    @Override
    public Stream<Integer> locate(String input) {
        List<Integer> indices = new ArrayList<>();
        int index = input.indexOf(delimiter);

        while (index != -1) {
            indices.add(index);
            index = input.indexOf(delimiter, index + 1);
        }

        return indices.stream();
    }
}
