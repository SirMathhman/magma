package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;

import java.util.ArrayList;
import java.util.List;

public class ValueSplitter implements Splitter {
    private static void advance(StringBuilder buffer, ArrayList<String> lines) {
        if (!buffer.isEmpty()) lines.add(buffer.toString());
    }

    @Override
    public List<String> split(String input) {
        var lines = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (c == ',' && depth == 0) {
                advance(buffer, lines);
                buffer = new StringBuilder();
            } else {
                if (c == '<' || c == '(') depth++;
                if (c == '>' || c == ')') depth--;
                buffer.append(c);
            }
        }
        advance(buffer, lines);
        return lines;
    }
}
