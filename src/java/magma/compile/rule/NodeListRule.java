package magma.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record NodeListRule(String propertyKey, Rule childRule) implements Rule {
    static Result<List<String>, CompileError> split(String root) {
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
                } else {
                    if (c == '{') depth++;
                    if (c == '}') depth--;
                }
            }
        }

        if (depth != 0) {
            return new Err<>(new CompileError("Invalid depth", new StringContext(root)));
        }

        advance(buffer, segments);
        return new Ok<>(segments);
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var children = node.findNodeList(getPropertyKey()).orElseThrow();

        Result<StringBuilder, CompileError> result = new Ok<>(new StringBuilder());
        for (var child : children) {
            result = result.and(() -> childRule().generate(child))
                    .mapValue(tuple -> tuple.left().append(tuple.right()));
        }

        return result.mapValue(StringBuilder::toString);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        final var split = split(input);
        return split.flatMapValue(segments -> {
            Result<List<Node>, CompileError> result1 = new Ok<>(new ArrayList<>());
            for (String segment : segments) {
                result1 = result1.and(() -> childRule().parse(segment.strip()))
                        .mapValue(tuple -> {
                            tuple.left().add(tuple.right());
                            return tuple.left();
                        });
            }
            return result1;
        }).mapValue(nodes -> new Node().withNodeList(propertyKey(), nodes));
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}