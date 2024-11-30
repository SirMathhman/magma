package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.app.compile.error.StringContext;
import magma.app.compile.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (input.endsWith(suffix)) {
            return childRule.parse(input.substring(0, input.length() - suffix.length()));
        }

        return new Err<>(new CompileError("Suffix '" + suffix + "' not present", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(slice -> slice + suffix);
    }
}
