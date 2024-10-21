package magma.app.compile.lang;

import magma.app.compile.rule.Locator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record BackwardsLocator(String delimiter) implements Locator {
    @Override
    public String slice() {
        return delimiter;
    }

    @Override
    public Stream<Integer> locate(String input) {
        List<Integer> indices = new ArrayList<>();
        int index = input.lastIndexOf(delimiter);

        while (index != -1) {
            indices.add(index);
            index = input.lastIndexOf(delimiter, index - 1);
        }

        return indices.stream();
    }
}
