package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.util.Collections;
import java.util.Optional;

public record ExtractRule(String propertyKey) implements Rule {
    private Optional<Node> parse0(String input) {
        return Optional.of(new MapNode().withString(propertyKey, input));
    }

    private Optional<String> generate0(Node node) {
        return Optional.of(node.findString(propertyKey).orElse(""));
    }

    private Result<Node, ParseException> parse1(String input) {
        return parse0(input)
                .<Result<Node, ParseException>>map(Ok::new)
                .orElseGet(() -> new Err<Node, ParseException>(new ParseException("Unknown input", input)));
    }

    private Result<String, GenerateException> generate1(Node node) {
        return generate0(node)
                .<Result<String, GenerateException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new GenerateException("Unknown node", node)));
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return new RuleResult<>(Collections.singletonList(parse1(input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return new RuleResult<>(Collections.singletonList(generate1(node)));
    }
}
