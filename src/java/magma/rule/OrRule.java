package magma.rule;

import magma.CompileException;
import magma.Node;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    private Optional<Node> parse1(String input) {
        return rules.stream()
                .map(rule -> rule.parse(input).findValue())
                .flatMap(Optional::stream)
                .findFirst();
    }

    private Optional<String> generate1(Node node) {
        return rules.stream()
                .map(rule -> rule.generate(node).findValue())
                .flatMap(Optional::stream)
                .findFirst();
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
