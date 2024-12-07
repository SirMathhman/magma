package magma;

import java.util.Optional;

public record InfixRule(Rule leftRule, String infix, Rule rightRule) implements Rule {
    @Override
    public Optional<Node> parse(String input) {
        final var contentIndex = input.indexOf(infix());
        if (contentIndex == -1) return Optional.empty();
        final var beforeContent = input.substring(0, contentIndex);
        final var parsed = leftRule().parse(beforeContent);

        final var afterContent = input.substring(contentIndex + infix().length()).strip();
        final var parsed1 = rightRule().parse(afterContent);
        return parsed.flatMap(inner -> parsed1.map(inner::merge));
    }

    @Override
    public Optional<String> generate(Node node) {
        return leftRule().generate(node).flatMap(left -> rightRule().generate(node).map(right -> left + infix() + right));
    }
}