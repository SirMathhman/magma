package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    private Optional<Node> parse0(String input) {
        for (Rule rule : rules) {
            final var parsed = rule.parse(input).findValue();
            if (parsed.isPresent()) return parsed;
        }
        return Optional.empty();
    }

    private Optional<String> generate0(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node).findValue();
            if (generated.isPresent()) return generated;
        }
        return Optional.empty();
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
