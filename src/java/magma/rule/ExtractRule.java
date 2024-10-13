package magma.rule;

import magma.GenerateException;
import magma.MapNode;
import magma.Node;
import magma.ParseException;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

public record ExtractRule(String propertyKey) implements Rule {
    private Optional<Node> parse0(String input) {
        Node node = new MapNode();
        return Optional.of(node.strings().with(propertyKey, input));
    }

    private Optional<String> generate0(Node node) {
        return Optional.of(node.strings().find(propertyKey).orElse(""));
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
