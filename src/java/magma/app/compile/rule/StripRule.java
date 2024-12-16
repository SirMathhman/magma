package magma.app.compile.rule;

import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.error.FormattedError;

public record StripRule(Rule childRule) implements Rule {
    @Override
    public Result<Node, FormattedError> parse(String input) {
        return childRule.parse(input.strip());
    }

    @Override
    public Result<String, FormattedError> generate(Node node) {
        return childRule.generate(node);
    }
}
