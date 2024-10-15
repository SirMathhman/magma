package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.ParseException;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

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
