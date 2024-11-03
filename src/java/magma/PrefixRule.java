package magma;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Option<MapNode> parse(String input) {
        if (!input.startsWith(prefix())) return new None<>();
        final var slice = input.substring(prefix().length());

        return childRule().parse(slice);
    }

    @Override
    public Option<String> generate(MapNode node) {
        return childRule.generate(node).map(value -> prefix + value);
    }
}