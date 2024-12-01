package magma.app.compile.lang.magma;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.java.JavaList;

public class CommonLang {
    public static final String NUMERIC_TYPE_TYPE = "numeric-type";
    public static final String NUMERIC_VALUE_TYPE = "numeric-value";
    public static final String NUMERIC_VALUE = "value";
    public static final String GROUP_TYPE = "group";
    public static final String GROUP_CHILDREN = "children";

    public static Node toGroup(JavaList<Node> children) {
        return new MapNode(GROUP_TYPE)
                .withNodeList(GROUP_CHILDREN, children);
    }
}
