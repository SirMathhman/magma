package magma.rule;

import magma.GenerateException;
import magma.Node;
import magma.ParseException;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

public final class SuffixRule implements Rule {
    private final String suffix;
    private final Rule childRule;

    public SuffixRule(String suffix, Rule childRule) {
        this.suffix = suffix;
        this.childRule = childRule;
    }

    private Optional<Node> parse0(String input) {
        if (!input.endsWith(suffix)) return Optional.empty();
        final var slice = input.substring(0, input.length() - suffix.length());
        return childRule.parse(slice).findValue();
    }

    private Optional<String> generate0(Node node) {
        return childRule.generate(node).findValue().map(value -> value + suffix);
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