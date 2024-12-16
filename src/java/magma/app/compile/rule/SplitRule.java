package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.collect.MutableList;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;

import java.util.ArrayList;

public final class SplitRule implements Rule {
    private final String propertyKey;
    private final Rule segmentRule;

    public SplitRule(String propertyKey, Rule segmentRule) {
        this.propertyKey = propertyKey;
        this.segmentRule = segmentRule;
    }

    private static java.util.List<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public Result<Node, FormattedError> parse(String root) {
        return Streams.from(split(root))
                .<Result<List<Node>, FormattedError>>foldLeft(new Ok<>(new MutableList<>()), (current, s) -> current.flatMapValue(inner -> segmentRule.parse(s).mapValue(inner::add)))
                .mapValue(nodes -> new MapNode().withNodeList(propertyKey, nodes));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return node.findNodeList(propertyKey)
                .map(this::generateAllIntoBuilder)
                .orElseGet(() -> createErr(node));
    }

    private Err<String, FormattedError> createErr(Node node) {
        final var format = "Node list '%s' not present";
        final var message = format.formatted(propertyKey);
        final var context = new NodeContext(node);
        return new Err<>(new CompileError(message, context));
    }

    private Result<String, FormattedError> generateAllIntoBuilder(List<Node> list) {
        return list.stream()
                .foldLeft(new Ok<>(new StringBuilder()), this::generateIntoBuilder)
                .mapValue(StringBuilder::toString);
    }

    private Result<StringBuilder, FormattedError> generateIntoBuilder(
            Result<StringBuilder, FormattedError> current,
            Node next
    ) {
        return current.flatMapValue(inner -> segmentRule.generate(next).mapValue(inner::append));
    }
}