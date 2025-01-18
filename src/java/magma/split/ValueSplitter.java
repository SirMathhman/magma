package magma.split;

import java.util.ArrayList;
import java.util.List;

public class ValueSplitter implements Splitter {
    @Override
    public StringBuilder merge(StringBuilder current, String element) {
        if (current.isEmpty()) return current.append(element);
        return current.append(", ").append(element);
    }

    @Override
    public List<String> split(String content) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            final var c = content.charAt(i);
            if (c == ',') {
                Splitter.advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
            }
        }

        Splitter.advance(segments, buffer);
        return segments;
    }
}
