package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record ExtractRule(String propertyKey) implements Rule {

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        if (input.isEmpty()) return new RuleResult<>(new Err<>(new ParseException("Input is empty", input)));
        return new RuleResult<>(new Ok<>(new MapNode().withString(propertyKey, input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return node.findString(propertyKey)
                .<RuleResult<String, GenerateException>>map(s -> new RuleResult<>(new Ok<>(s)))
                .orElseGet(() -> new RuleResult<>(new Err<>(new GenerateException("String '" + propertyKey + "' not present", node))));

    }
}
