package magma.java;

import magma.app.compile.Node;

import java.util.ArrayList;
import java.util.List;

public class JavaLists {
    public static List<Node> add(List<Node> list, Node element) {
        var copy = new ArrayList<>(list);
        copy.add(element);
        return copy;
    }
}
