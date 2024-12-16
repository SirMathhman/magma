package magma.app.rule;

import magma.app.error.StringContext;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return input.startsWith(prefix)
                ? childRule.parse(input.substring(prefix.length()))
                : new Err<>(new CompileError("Prefix '" + prefix + "' not present", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(generated -> prefix + generated);
    }
}
