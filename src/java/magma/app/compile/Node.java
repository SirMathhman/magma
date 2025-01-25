package magma.app.compile;

import magma.java.JavaList;

public interface Node {
    NodeProperties<Input> inputs();

    NodeProperties<Node> nodes();

    NodeProperties<JavaList<Node>> nodeLists();

    String format(int depth);

    Node merge(Node other);

    String display();

    Node retype(String type);

    boolean is(String type);

    boolean hasType();
}
