package magma;

import java.util.Optional;

public record InfixRule(Rule leftRule, String infix, Rule rightRule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        final var index = input.indexOf(input);
        if (index == -1) return Optional.empty();

        final var leftSlice = input.substring(0, index);
        final var rightSlice = input.substring(index + infix.length());

        return leftRule.parse(leftSlice).flatMap(parsedLeft -> rightRule.parse(rightSlice).map(parsedLeft::merge));
    }

    @Override
    public Optional<String> generate(Node node) {
        return leftRule.generate(node).flatMap(generatedLeft -> rightRule.generate(node).map(generatedRight -> generatedLeft + infix + generatedRight));
    }
}
