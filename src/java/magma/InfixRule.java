package magma;

import java.util.Optional;

public record InfixRule(Rule leftRule, String infix, Rule rightRule) implements Rule {
    @Override
    public Optional<String> generate(Node node) {
        return leftRule().generate(node).flatMap(left -> rightRule().generate(node).map(right -> left + infix() + right));
    }
}