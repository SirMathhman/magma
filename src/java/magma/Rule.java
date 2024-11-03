package magma;

public interface Rule {
    Option<MapNode> parse(String input);

    Option<String> generate(MapNode node);
}
