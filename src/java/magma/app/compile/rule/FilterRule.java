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
        if (filter.filter(input)) return childRule.parse(input);
        return new RuleResult<>(new Err<>(new ParseException("Value did not pass filter '" + filter + "'", input)));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return childRule.generate(node);
    }
}
