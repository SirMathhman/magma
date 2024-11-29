package magma.app.compile.rule;

import java.util.ArrayList;
import java.util.List;

public class ValueSplitter implements Splitter {
    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (c == ',') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
            }
        }
        advance(buffer, segments);
        return segments;
    }
}
