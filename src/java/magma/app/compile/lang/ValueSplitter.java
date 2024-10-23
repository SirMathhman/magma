package magma.app.compile.lang;

import magma.app.compile.rule.Splitter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValueSplitter implements Splitter {
    private static void advance(StringBuilder buffer, ArrayList<String> lines) {
        if (!buffer.isEmpty()) lines.add(buffer.toString());
    }

    @Override
    public List<String> split(String input) {
        var lines = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        final var length = input.length();
        var queue = IntStream.range(0, length)
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            var c = queue.pop();
            if (c == '\"') {
                buffer.append(c);

                while (!queue.isEmpty()) {
                    final var next = queue.pop();
                    buffer.append(next);

                    if (next == '\\' && !queue.isEmpty()) {
                        final var escaped = queue.pop();
                        buffer.append(escaped);
                    }

                    if (next == '\"') {
                        break;
                    }
                }
            } else if (c == '-') {
                buffer.append('-');
                if (!queue.isEmpty() && queue.peek() == '>') {
                    queue.pop();
                }
            } else if (c == ',' && depth == 0) {
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
