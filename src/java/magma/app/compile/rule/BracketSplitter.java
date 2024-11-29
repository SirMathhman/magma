package magma.app.compile.rule;

import java.util.ArrayList;
import java.util.List;

public class BracketSplitter implements Splitter {
    @Override
    public List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                if (!buffer.isEmpty()) segments.add(buffer.toString());
                buffer = new StringBuilder();
            }
        }
        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return segments;
    }
}