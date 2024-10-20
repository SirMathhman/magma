package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;

import java.util.Arrays;
import java.util.List;

public class ValueSplitter implements Splitter {
    @Override
    public List<String> split(String input) {
        return Arrays.asList(input.split(","));
    }
}
