package magma.app.rule;

import magma.app.error.StringContext;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return input.endsWith(suffix)
                ? childRule.parse(input.substring(0, input.length() - suffix.length()))
                : new Err<>(new CompileError("Suffix '" + suffix + "' not present", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule().generate(node).mapValue(generated -> generated + suffix());
    }
}