package magma.app.compile.rule.locate;

import magma.api.stream.Stream;
import magma.api.stream.Streams;

import java.util.ArrayList;
import java.util.List;

public class BackwardsLocator implements Locator {
    private final String infix;

    public BackwardsLocator(String infix) {
        this.infix = infix;
    }

    @Override
    public String unwrap() {
        return this.infix;
    }

    @Override
    public int length() {
        return this.infix.length();
    }

    @Override
    public Stream<Integer> locate(String input) {
        return Streams.fromNativeList(searchForIndices(input));
    }

    private List<Integer> searchForIndices(String input) {
        List<Integer> indices = new ArrayList<>();
        int index = input.lastIndexOf(this.infix);
        while (index >= 0) {
            indices.add(index);
            index = input.lastIndexOf(this.infix, index - 1);
        }
        return indices;
    }
}
