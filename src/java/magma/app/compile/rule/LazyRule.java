package magma.app.compile.rule;

import magma.api.result.Err;
import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.Optional;

public class LazyRule implements Rule {
    private Optional<Rule> childRule = Optional.empty();

    public void setChildRule(Rule childRule) {
        this.childRule = Optional.of(childRule);
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        if (childRule.isEmpty()) return new RuleResult<>(new Err<>(new ParseException("Child rule is not set", input)));
        return childRule.orElseThrow().parse(input);
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        if (childRule.isEmpty())
            return new RuleResult<>(new Err<>(new GenerateException("Child rule is not set", node)));
        return childRule.orElseThrow().generate(node);
    }
}
