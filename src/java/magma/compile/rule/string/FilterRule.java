package magma.compile.rule.string;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.compile.rule.Rule;
import magma.compile.rule.string.filter.Filter;

public final class FilterRule implements Rule {
    private final Rule childRule;
    private final Filter filter;

    public FilterRule(Filter filter, Rule childRule) {
        this.childRule = childRule;
        this.filter = filter;
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        if (filter.test(input)) return childRule.parse(input);
        else return new Err<>(new CompileError(filter.createErrorMessage(), new StringContext(input)));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node);
    }
}
