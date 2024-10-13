package magma;

import magma.rule.Properties;

import java.util.List;

public interface Node {
    Properties<String> strings();

    Properties<List<Node>> nodes();

    Node retype(String type);

    boolean is(String type);
}
