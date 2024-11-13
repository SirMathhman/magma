package magma.app.compile.rule;

import java.util.Arrays;
import java.util.List;

public class ValueSplitter implements Splitter {
    @Override
    public List<String> split(String input) {
        return Arrays.stream(input.split(",")).toList();
    }
}
