package magma.app.compile.node;

import magma.api.option.Option;
import magma.java.JavaList;

public interface Node {
    Option<String> mergeType(Option<String> otherType, MergeStrategy strategy);

    NodeProperties<Input> inputs();

    NodeProperties<Node> nodes();

    NodeProperties<JavaList<Node>> nodeLists();

    String format(int depth);

    Node merge(Node other, MergeStrategy strategy);

    String display();

    Node retype(String type);

    boolean is(String type);

    boolean hasType();
}
