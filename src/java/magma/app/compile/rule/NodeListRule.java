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

public record NodeListRule(String propertyKey, Divider divider, Rule segmentRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return divider.divide(input)
                .flatMapValue(this::processDivided)
                .mapValue(nodes -> new MapNode().withNodeList(propertyKey, nodes));
    }

    private Result<List<Node>, FormattedError> processDivided(List<Input> divided) {
        return divided.stream().<Result<List<Node>, FormattedError>>foldLeft(new Ok<>(new MutableJavaList<>()),
                (current, input) -> current.flatMapValue(inner -> segmentRule.parse(input).mapValue(inner::add)));
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
                .map(buffer -> divider.concat(buffer, slice))
                .orElseGet(() -> new StringBuilder().append(slice)));
    }
}