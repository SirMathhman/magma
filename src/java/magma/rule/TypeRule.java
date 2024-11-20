package magma.rule;

import magma.CompileException;
import magma.Node;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

public record TypeRule(String type, Rule childRule) implements Rule {
    private Optional<Node> parse1(String input) {
        return childRule.parse(input).findValue().map(node -> node.retype(type));
    }

    private Optional<String> generate1(Node node) {
        if(!node.is(type)) return Optional.empty();
        return childRule.generate(node).findValue();
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
