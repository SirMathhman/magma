package magma;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Option<MapNode> parse(String input) {
        if (!input.endsWith(suffix)) return new None<>();
        final var value = input.substring(0, input.length() - suffix.length());

        return childRule().parse(value);
    }

    @Override
    public Option<String> generate(MapNode node) {
        return childRule.generate(node).map(value -> value + suffix);
    }
}