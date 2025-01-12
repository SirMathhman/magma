package magma.split;

import magma.collect.List;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.Strings;

public class StatementSplitter implements Splitter {
    @Override
    public StringBuilder merge(StringBuilder inner, String compiled) {
        return inner.append(compiled);
    }

    @Override
    public List<String> split(String input) {
        var segments = new JavaList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = Strings.streamChars(input).collect(JavaLinkedList.collector());

        while (!queue.isEmpty()) {
            var c = queue.popOrPanic();
            buffer.append(c);

            if (c == '\'') {
                final var popped = queue.popOrPanic();
                buffer.append(popped);
                if (popped == '\\') {
                    buffer.append(queue.popOrPanic());
                }

                buffer.append(queue.popOrPanic());
                continue;
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var next = queue.popOrPanic();
                    buffer.append(next);

                    if (next == '"') break;
                    if (next == '\\') {
                        buffer.append(queue.popOrPanic());
                    }
                }
            }

            if (c == ';' && depth == 0) {
                Splitter.advance(segments, buffer);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                Splitter.advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                if (c == '{' || c == '(') depth++;
                if (c == '}' || c == ')') depth--;
            }
        }
        Splitter.advance(segments, buffer);
        return segments;
    }
}
