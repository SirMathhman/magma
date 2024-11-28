package magma.rule;

import magma.Node;
import magma.error.StringContext;
import magma.error.CompileError;
import magma.result.Err;
import magma.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if(input.startsWith(prefix)) {
           return childRule.parse(input.substring(prefix.length()));
        }

        return new Err<>(new CompileError("Prefix '" + prefix + "' not present", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(slice -> prefix + slice);
    }
}
