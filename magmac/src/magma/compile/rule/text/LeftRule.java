package magma.compile.rule.text;

import magma.compile.attribute.Attributes;
import magma.compile.rule.Node;
import magma.compile.rule.Rule;
import magma.compile.rule.result.AdaptiveRuleResult;
import magma.compile.rule.result.RuleResult;

import java.util.Optional;

public record LeftRule(String slice, Rule child) implements Rule {
    private Optional<Attributes> toNode0(String input) {
        if (!input.startsWith(slice)) return Optional.empty();
        var content = input.substring(slice.length());
        return child.toNode(content).findAttributes();
    }

    private Optional<String> fromNode0(Attributes attributes) {
        return child.fromNode(new Node("", attributes)).map(inner -> slice + inner);
    }

    @Override
    public RuleResult toNode(String input) {
        return new AdaptiveRuleResult(Optional.empty(), toNode0(input));
    }

    @Override
    public Optional<String> fromNode(Node node) {
        return fromNode0(node.attributes());
    }
}