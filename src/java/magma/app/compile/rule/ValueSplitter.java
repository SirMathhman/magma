package magma.app.compile.rule;

import java.util.ArrayList;
import java.util.List;

public class ValueSplitter implements Splitter {
    @Override
    public List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        var length = input.length();
        for (int i = 0; i < length; i++) {
            var c = input.charAt(i);
            if (c == ',' && depth == 0) {
                if (!buffer.isEmpty()) segments.add(buffer.toString());
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
                if (c == '[') depth++;
                if (c == ']') depth--;
            }
        }

        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return segments;
    }
}
