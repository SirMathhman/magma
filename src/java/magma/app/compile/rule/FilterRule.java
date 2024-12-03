package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;

public record FilterRule(Filter filter, Rule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if(filter.test(input)) return rule.parse(input);
        return new Err<>(new CompileError("Filter cannot qualify", new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return rule.generate(node);
    }
}
