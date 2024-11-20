package magma.rule;

import magma.CompileException;
import magma.Node;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

public record PrefixRule(String prefix, Rule childRule) implements Rule {

    private Optional<Node> parse1(String input) {
        if (!input.startsWith(this.prefix())) return Optional.empty();
        final var substring = input.substring(this.prefix().length());
        return this.childRule().parse(substring).findValue();
    }

    private Optional<String> generate1(Node node) {
        return childRule.generate(node).findValue().map(slice -> prefix + slice);
    }

    @Override
    public Result<Node, CompileException> parse(String input) {
        return parse1(input)
                .<Result<Node, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException()));
    }

    @Override
    public Result<String, CompileException> generate(Node node) {
        return generate1(node)
                .<Result<String, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException()));
    }
}