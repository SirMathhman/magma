package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.Input;
import magma.app.compile.Node;
import magma.app.error.FormattedError;

public record StripRule(Rule childRule, String beforeChildKey, String afterChildKey) implements Rule {
    public StripRule(Rule childRule) {
        this(childRule, "", "");
    }

    private Result<Node, FormattedError> parse0(String input) {
        String input1 = input.strip();
        return childRule.parse(new Input(input1, 0, input1.length()));

    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        final var beforeChild = node.findString(beforeChildKey).orElseGet(() -> "");
        final var afterChild = node.findString(afterChildKey).orElseGet(() -> "");
        return childRule.generate(node).mapValue(generated -> beforeChild + generated + afterChild);
    }

    @Override
    public Result<Node, FormattedError> parse(Input input) {
        return parse0(input.slice());
    }
}
