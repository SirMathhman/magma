package magma;

import java.util.Optional;

public record InfixRule(Rule leftRule, String infix, Rule rightRule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        final var index = input.indexOf(infix());
        if (index == -1) return Optional.empty();

        final var leftResult = leftRule().parse(input.substring(0, index));
        final var rightResult = rightRule().parse(input.substring(index + infix().length()));

        return leftResult.flatMap(left -> rightResult.map(left::merge));
    }
}