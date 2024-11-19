package magma;

import java.util.List;
import java.util.Optional;

public record OrRule(List<Rule> rules) implements Rule {
    private Optional<String> parse0(String input) {
        return rules.stream()
                .map(rule -> rule.parse(input).map(node -> node.findValue(Compiler.VALUE).orElseThrow()))
                .flatMap(Optional::stream)
                .findFirst();
    }

    @Override
    public Optional<Node> parse(String input) {
        return parse0(input).map(value -> new Node(Optional.empty()).withString(Compiler.VALUE, value));
    }

    @Override
    public Optional<String> generate(Node node) {
        for (Rule rule : rules) {
            final var generated = rule.generate(node);
            if(generated.isPresent()) return generated;
        }

        return Optional.empty();
    }
}
