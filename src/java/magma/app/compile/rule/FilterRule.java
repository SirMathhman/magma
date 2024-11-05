package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;

public record FilterRule(Filter filter, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!filter.validate(input)) {
            return new Err<>(new CompileError("Input did not pass filter - " + filter.createMessage(), new StringContext(input)));
        }

        return childRule.parse(input);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node);
    }
}
