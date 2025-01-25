package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.compile.rule.divide.DivideRule;

import java.util.List;

public final class OptionalNodeListRule implements Rule {
    private final String propertyKey;
    private final Rule ifPresent;
    private final Rule ifEmpty;
    private final OrRule rule;

    public OptionalNodeListRule(String propertyKey, Rule ifPresent, Rule ifEmpty) {
        this.propertyKey = propertyKey;
        this.ifPresent = ifPresent;
        this.ifEmpty = ifEmpty;
        this.rule = new OrRule(List.of(ifPresent, ifEmpty));
    }

    public OptionalNodeListRule(String propertyKey, DivideRule ifPresent) {
        this(propertyKey, ifPresent, new ExactRule(""));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.rule.parse(input);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (node.hasNodeList(this.propertyKey)) {
            return this.ifPresent.generate(node);
        } else {
            return this.ifEmpty.generate(node);
        }
    }
}
