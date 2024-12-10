package magma;

import java.util.Optional;
import java.util.function.Function;

public record InfixRule(Rule leftRule, String slice,
                        Rule rightRule) implements Rule {
    @Override
    public Optional<Node> parse(
            String input) {
        final var operator = input.indexOf(slice());
        if (operator == -1) return Optional.empty();

        final var left = input.substring(0, operator);
        final var right = input.substring(operator + 1);

        return leftRule().parse(left).flatMap(leftValue -> rightRule()
                .parse(right)
                .map(leftValue::merge));
    }
}