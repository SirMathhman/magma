package magma.compile.rule.split.locate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BackwardsLocator implements Locator {
    @Override
    public Stream<Integer> locate(String input, String infix) {
        // List to hold indices
        List<Integer> indices = new ArrayList<>();

        // Find indices of all occurrences of infix
        int index = input.lastIndexOf(infix);
        while (index != -1) {
            indices.add(index);
            index = input.lastIndexOf(infix, index - 1);
        }

        // Return indices as a stream
        return indices.stream();
    }
}
