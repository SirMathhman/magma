package magma.split;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatementSplitter implements Splitter {
    @Override
    public StringBuilder merge(StringBuilder current, String element) {
        return current.append(element);
    }

    @Override
    public List<String> split(String content) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, content.length())
                .mapToObj(content::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                final var c1 = queue.pop();
                buffer.append(c1);

                if (c1 == '\\') buffer.append(queue.pop());
                buffer.append(queue.pop());
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var c1 = queue.pop();
                    buffer.append(c1);

                    if (c1 == '"') break;
                    if (c1 == '\\') buffer.append(queue.pop());
                }
            }

            if (c == ';' && depth == 0) {
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }

        advance(segments, buffer);
        if (depth != 0) {
            System.err.println("Invalid depth: '" + depth + "': " + content);
        }

        return segments;
    }
}
