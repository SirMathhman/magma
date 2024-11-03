package magma.compile.rule;

import magma.compile.error.CompileError;
import magma.compile.Node;
import magma.compile.error.StringContext;
import magma.compile.error.NodeContext;
import magma.option.None;
import magma.option.Option;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    private Option<Node> parse0(String input) {
        if (!input.endsWith(suffix)) return new None<>();
        final var value = input.substring(0, input.length() - suffix.length());

        return this.childRule().parse(value).findValue();
    }

    private Option<String> generate0(Node node) {
        return childRule.generate(node).findValue().map(value -> value + suffix);
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return parse0(input)
                .<Result<Node, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Invalid input", new StringContext(input))));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return generate0(node)
                .<Result<String, CompileError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileError("Invalid node", new NodeContext(node))));
    }
}