package magma.app.compile.rule;

import magma.app.error.FormattedError;
import magma.app.error.StringContext;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.api.result.Err;
import magma.api.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return input.input().endsWith(suffix)
                ? childRule.parse(new Input(input.input().substring(0, input.input().length() - suffix.length())))
                : new Err<>(new CompileError("Suffix '" + suffix + "' not present", new StringContext(input.input())));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule().generate(node).mapValue(generated -> generated + suffix());
    }
}