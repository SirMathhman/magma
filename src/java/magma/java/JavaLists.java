package magma.java;

import magma.api.Tuple;
import magma.app.compile.Node;

import java.util.ArrayList;
import java.util.List;

public class JavaLists {
    public static List<Node> add(Tuple<List<Node>, Node> tuple) {
        final var left = new ArrayList<>(tuple.left());
        left.add(tuple.right());
        return left;
    }
}
