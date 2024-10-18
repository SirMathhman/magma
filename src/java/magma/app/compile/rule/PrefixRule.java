package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        if (!input.startsWith(prefix))
            return new RuleResult<>(new Err<>(new ParseException("Prefix '" + prefix + "' not present", input)));

        return childRule.parse(input.substring(prefix.length()));
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        return childRule.generate(node).mapValue(value -> prefix + value);
    }
}