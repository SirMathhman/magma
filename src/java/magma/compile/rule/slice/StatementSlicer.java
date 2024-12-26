package magma.compile.rule.slice;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StatementSlicer implements Slicer {
    public static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public StringBuilder merge(StringBuilder builder, String value) {
        return builder.append(value);
    }

    @Override
    public Result<List<String>, CompileError> slice(String root) {
        var queue = IntStream.range(0, root.length())
                .mapToObj(root::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        while (!queue.isEmpty()) {
            var c = queue.pop();
            buffer.append(c);
            if (c == '\'' && !queue.isEmpty()) {
                final var next = queue.pop();
                buffer.append(next);

                if (next == '\\') {
                    if (!queue.isEmpty()) buffer.append(queue.pop());
                }

                if (!queue.isEmpty()) buffer.append(queue.pop());
            } else if (c == '\"') {
                while (!queue.isEmpty()) {
                    final var next = queue.pop();
                    buffer.append(next);

                    if (next == '\"') {
                        break;
                    } else if (next == '\\') {
                        if (!queue.isEmpty()) {
                            buffer.append(queue.pop());
                        }
                    }
                }
            } else {
                if (c == ';' && depth == 0) {
                    advance(buffer, segments);
                    buffer = new StringBuilder();
                } else if (c == '}' && depth == 1) {
                    depth--;
                    advance(buffer, segments);
                    buffer = new StringBuilder();
                } else {
                    if (c == '{' || c == '(') depth++;
                    if (c == '}' || c == ')') depth--;
                }
            }
        }

        if (depth != 0) {
            return new Err<List<String>, CompileError>(new CompileError("Invalid depth", new StringContext(root)));
        }

        advance(buffer, segments);
        return new Ok<List<String>, CompileError>(segments);
    }
}