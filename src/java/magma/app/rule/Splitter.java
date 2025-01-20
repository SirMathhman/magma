package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Splitter {
    public static Result<List<String>, CompileError> splitByStatements(String input) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                final var c1 = queue.pop();
                buffer.append(c1);

                if (c1 == '\\') {
                    buffer.append(queue.pop());
                }
                buffer.append(queue.pop());
                continue;
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var c1 = queue.pop();
                    buffer.append(c1);

                    if (c1 == '"') break;
                    if (c1 == '\\') {
                        buffer.append(queue.pop());
                    }
                }

                continue;
            }

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
        advance(buffer, segments);

        if (depth == 0) {
            return new Ok<>(segments);
        } else {
            return new Err<>(new CompileError("Invalid depth '" + depth + "'", new StringContext(input)));
        }
    }

    public static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    public static Result<List<String>, CompileError> splitByValues(String input) {
        final var segments = new ArrayList<String>();
        final var buffer = new StringBuilder();
        var depth = 0;
        int i = 0;
        while (i < input.length()) {
            final var c = input.charAt(i);
            if (c == ',' && depth == 0) {
                advance(buffer, segments);
            } else {
                buffer.append(c);
                if (c == '<') depth++;
                if (c == '>') depth--;
            }
            i++;
        }

        advance(buffer, segments);
        return new Ok<>(segments);
    }
}
