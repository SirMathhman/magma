package magma.compile.rule;

import magma.compile.Node;
import magma.option.Option;

public interface Rule {
    Option<Node> parse(String input);

    Option<String> generate(Node node);
}
