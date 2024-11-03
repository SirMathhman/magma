package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;
import magma.result.Err;
import magma.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!input.startsWith(this.prefix()))
            return new Err<>(new CompileError("Prefix '" + prefix + "' not present", new StringContext(input)));
        final var slice = input.substring(this.prefix().length());

        return this.childRule().parse(slice);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(value -> prefix + value);
    }
}