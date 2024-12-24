package magma.compile.rule;

import magma.compile.Node;
import magma.compile.error.CompileError;
import magma.compile.error.StringContext;
import magma.api.result.Err;
import magma.api.result.Result;

public record InfixRule(Rule leftRule, String infix, Rule rightRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        final var index = input.indexOf(input);
        if (index == -1) return new Err<>(new CompileError("Infix '" + infix + "' not present", new StringContext(input)));

        final var leftSlice = input.substring(0, index);
        final var rightSlice = input.substring(index + infix.length());

        return leftRule.parse(leftSlice).flatMapValue(parsedLeft -> rightRule.parse(rightSlice).mapValue(parsedLeft::merge));
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        return leftRule.generate(node).flatMapValue(generatedLeft -> rightRule.generate(node).mapValue(generatedRight -> generatedLeft + infix + generatedRight));
    }
}
