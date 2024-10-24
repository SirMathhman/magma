package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public final class SuffixRule implements Rule {
    private final String suffix;
    private final Rule childRule;

    public SuffixRule(Rule childRule, String suffix) {
        this.suffix = suffix;
        this.childRule = childRule;
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        if (!input.endsWith(suffix))
            return new RuleResult<>(new Err<>(new ParseException("Suffix '" + suffix + "' not present", input)));
        final var slice = input.substring(0, input.length() - suffix.length());
        return childRule.parseWithTimeout(slice);
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return childRule.generate(node).mapValue(value -> value + suffix);
    }
}