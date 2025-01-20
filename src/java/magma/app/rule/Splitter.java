package magma.app.rule;

import java.util.ArrayList;

public class Splitter {
    public static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
