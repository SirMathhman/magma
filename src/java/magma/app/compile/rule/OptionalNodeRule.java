package magma.app.compile.rule;

import magma.app.compile.GenerateException;
import magma.app.compile.Node;
import magma.app.compile.ParseException;

import java.util.List;

public final class OptionalNodeRule implements Rule {
    private final String propertyKey;
    private final Rule ifPresent;
    private final Rule ifEmpty;
    private final OrRule maybe;

    public OptionalNodeRule(String propertyKey, Rule ifEmpty, Rule ifPresent) {
        this.propertyKey = propertyKey;
        this.ifPresent = ifPresent;
        this.ifEmpty = ifEmpty;
        maybe = new OrRule(List.of(ifPresent, ifEmpty));
    }

    @Override
    public RuleResult<Node, ParseException> parse(String input) {
        return maybe.parse(input);
    }

    @Override
    public RuleResult<String, GenerateException> generate(Node node) {
        if (node.hasNode(propertyKey)) {
            return ifPresent.generate(node);
        } else {
            return ifEmpty.generate(node);
        }
    }
}
