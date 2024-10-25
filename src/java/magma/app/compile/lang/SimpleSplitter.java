package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;

import java.util.List;
import java.util.regex.Pattern;

public class SimpleSplitter implements Splitter {
    private final String delimiter;

    public SimpleSplitter(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public List<String> split(String input) {
        return List.of(input.split(Pattern.quote(delimiter)));
    }
}
