package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        if (!input.endsWith(suffix()))
            return new Err<>(new CompileError("Suffix '" + suffix + "' not present", new StringContext(input)));
        final var slice = input.substring(0, input.length() - suffix().length());
        return childRule.parse(slice);
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return childRule().generate(node).mapValue(value -> value + suffix());
    }
}