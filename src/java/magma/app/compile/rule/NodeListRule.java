package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.FormattedError;
import magma.app.error.NodeContext;

public record NodeListRule(String propertyKey, Splitter splitter, Rule segmentRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(String root) {
        return splitter.split(root)
                .stream()
                .<Result<List<Node>, FormattedError>>foldLeft(new Ok<>(new MutableJavaList<>()), (current, s) -> current.flatMapValue(inner -> segmentRule.parse(s).mapValue(inner::add)))
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
                .foldLeft(new Ok<>(new None<>()), this::generateIntoBuilder)
                .mapValue(buffer -> buffer.map(StringBuilder::toString).orElseGet(() -> ""));
    }

    private Result<Option<StringBuilder>, FormattedError> generateIntoBuilder(
            Result<Option<StringBuilder>, FormattedError> current,
            Node next
    ) {
        return current.flatMapValue(inner -> segmentRule.generate(next).mapValue(slice -> mergeWithBuffer(inner, slice)));
    }

    private Some<StringBuilder> mergeWithBuffer(Option<StringBuilder> maybeBuffer, String slice) {
        return new Some<>(maybeBuffer
                .map(buffer -> splitter.merge(buffer, slice))
                .orElseGet(() -> new StringBuilder().append(slice)));
    }
}