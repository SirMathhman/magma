package magma;

import java.util.Optional;

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
