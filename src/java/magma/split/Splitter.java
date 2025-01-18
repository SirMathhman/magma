package magma.split;

import java.util.List;

public interface Splitter {
    static void advance(List<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    StringBuilder merge(StringBuilder current, String element);

    List<String> split(String content);
}
