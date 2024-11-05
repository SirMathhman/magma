package magma.app.compile.rule;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;

public record StripRule(Rule childRule, Option<String> leftPaddingKey, Option<String> rightPaddingKey) implements Rule {
    public StripRule(Rule childRule) {
        this(childRule, new None<>(), new None<>());
    }

    public StripRule(String leftPaddingKey, Rule childRule, String rightPaddingKey) {
        this(childRule, new Some<>(leftPaddingKey), new Some<>(rightPaddingKey));
    }

    @Override
    public Result<Node, CompileError> parse(String input) {
        return childRule.parse(input.strip());
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule.generate(node).mapValue(value -> {
            final var leftPadding = leftPaddingKey.flatMap(node::findString).orElse("");
            final var rightPadding = rightPaddingKey.flatMap(node::findString).orElse("");
            return leftPadding + value + rightPadding;
        });
    }
}
