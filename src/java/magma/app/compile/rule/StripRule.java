package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.error.FormattedError;

public record StripRule(Rule childRule, String beforeChildKey, String afterChildKey) implements Rule {
    public StripRule(Rule childRule) {
        this(childRule, "", "");
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return childRule.parse(new Input(input.input().strip()));
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        final var beforeChild = node.findString(beforeChildKey).orElseGet(() -> "");
        final var afterChild = node.findString(afterChildKey).orElseGet(() -> "");
        return childRule.generate(node).mapValue(generated -> beforeChild + generated + afterChild);
    }
}
