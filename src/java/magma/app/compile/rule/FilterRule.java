package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public final class FilterRule implements Rule {
    private final Rule childRule;
    private final Filter filter;

    public FilterRule(Filter filter, Rule childRule) {
        this.childRule = childRule;
        this.filter = filter;
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return filter.filter(input) ? childRule.parse(input) : new RuleResult<Node, ParseException>(new Err<>(new ParseException("Invalid value", input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return childRule.generate(node);
    }
}
