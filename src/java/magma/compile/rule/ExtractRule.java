package magma.compile.rule;

import magma.compile.GenerateException;
import magma.compile.MapNode;
import magma.compile.Node;
import magma.compile.ParseException;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

public record ExtractRule(String propertyKey) implements Rule {
    private Optional<Node> parse0(String input) {
        return Optional.of(new MapNode().withString(propertyKey, input));
    }

    private Optional<String> generate0(Node node) {
        return Optional.of(node.findString(propertyKey).orElse(""));
    }

    @Override
    public Result<Node, ParseException> parse(String input) {
        return parse0(input)
                .<Result<Node, ParseException>>map(Ok::new)
                .orElseGet(() -> new Err<Node, ParseException>(new ParseException("Unknown input", input)));
    }

    @Override
    public Result<String, GenerateException> generate(Node node) {
        return generate0(node)
                .<Result<String, GenerateException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new GenerateException("Unknown node", node)));
    }
}
