package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public RuleResult<Node, ParseException> parse(String input) {
        final var segments = splitter.split(input);
        if (segments.isEmpty()) {
            return new RuleResult<>(new Err<>(new ParseException("No segments present for node list '" + propertyKey + "'", input)));
        }

        var children = new ArrayList<Node>();
        int i = 0;
        while (i < segments.size()) {
            var segment = segments.get(i);
            final var result = childRule.parse(segment);
            final var inner = result.result();
            if (inner.isErr()) {
                return new RuleResult<>(new Err<>(new ParseException("Invalid child for property '" + propertyKey + "'", segment)), Collections.singletonList(result));
            }

            children.add(inner.findValue().orElseThrow());
            i++;
        }

        return new RuleResult<>(new Ok<>(new MapNode().withNodeList(propertyKey, children)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        final var propertyValues = node.findNodeList(propertyKey);
        if (propertyValues.isEmpty())
            return new RuleResult<>(new Err<>(new GenerateException("Node list property '" + propertyKey + "' not present", node)));

        var buffer = new StringBuilder();
        List<Node> get = propertyValues.get();
        int i = 0;
        while (i < get.size()) {
            var value = get.get(i);
            final var result = childRule.generate(value);
            final var inner = result.result();
            if (inner.isErr()) {
                return new RuleResult<>(new Err<>(new GenerateException("Invalid child", value)), Collections.singletonList(result));
            }

            final var str = inner.findValue().orElseThrow();
            buffer.append(str);
            i++;
        }

        return new RuleResult<>(new Ok<>(buffer.toString()));
    }
}