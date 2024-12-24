package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!input.startsWith(prefix))
            return new Err<>(new CompileError("Prefix '" + prefix + "' not present", new StringContext(input)));
        final var afterKeyword = input.substring(prefix.length());
        return childRule().parse(afterKeyword);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule().generate(node).mapValue(value -> prefix() + value);
    }
}