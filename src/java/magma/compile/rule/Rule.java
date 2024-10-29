package magma.compile.rule;

import magma.compile.Node;
import magma.core.String_;
import magma.core.option.Option;

public interface Rule {
    Option<Node> parse(String_ input);

    Option<String_> generate(Node node);
}
