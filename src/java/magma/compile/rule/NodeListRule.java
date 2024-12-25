package magma.compile.rule;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.rule.split.Splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NodeListRule implements Rule {
    private final String propertyKey;
    private final Rule childRule;
    private final Splitter splitter;

    public NodeListRule(Splitter splitter, String propertyKey, Rule childRule) {
        this.propertyKey = propertyKey;
        this.childRule = childRule;
        this.splitter = splitter;
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        final var children = node.findNodeList(propertyKey).orElseThrow();

        Result<Optional<StringBuilder>, CompileError> result = new Ok<>(Optional.empty());
        for (var child : children) {
            result = result.and(() -> childRule.generate(child)).mapValue(tuple -> {
                final var maybeBuilder = tuple.left();
                final var value = tuple.right();
                return Optional.of(maybeBuilder.map(builder -> splitter.merge(builder, value)).orElse(new StringBuilder().append(value)));
            });
        }

        return result.mapValue(builder -> builder.orElse(new StringBuilder())).mapValue(StringBuilder::toString);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return splitter.split(input)
                .flatMapValue(this::parseSegments)
                .mapValue(nodes -> new Node().withNodeList(propertyKey, nodes));
    }

    private Result<List<Node>, CompileError> parseSegments(List<String> segments) {
        Result<List<Node>, CompileError> result1 = new Ok<>(new ArrayList<>());
        for (String segment : segments) {
            result1 = result1.and(() -> childRule.parse(segment.strip()))
                    .mapValue(tuple -> {
                        tuple.left().add(tuple.right());
                        return tuple.left();
                    });
        }
        return result1;
    }
}