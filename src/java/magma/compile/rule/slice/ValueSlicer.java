package magma.compile.rule.slice;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.error.CompileError;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValueSlicer implements Slicer {
    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public StringBuilder merge(StringBuilder builder, String value) {
        return builder.append(", ").append(value);
    }

    @Override
    public Result<List<String>, CompileError> slice(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, root.length())
                .mapToObj(index -> root.charAt(index))
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            var c = queue.pop();

            if (c == ',' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);

                if (c == '-' && !queue.isEmpty()) {
                    final var next = queue.peek();
                    if (next == '>') {
                        buffer.append(queue.pop());
                    }
                }

                if (c == '<' || c == '(') depth++;
                if (c == '>' || c == ')') depth--;
            }
        }
        advance(buffer, segments);

        return new Ok<>(segments);
    }
}
