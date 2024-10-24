package magma.app.compile.pass;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;

import java.util.ArrayList;
import java.util.Optional;

public class RecordPasser {
    public static Optional<Node> passRecord(Node node) {
        if (!node.is(JavaLang.RECORD_TYPE)) return Optional.empty();

        final var retyped = node.retype(MagmaLang.FUNCTION);

        final var withImplements = retyped.mapNodeList(CommonLang.CHILDREN, children -> {
            var copy = new ArrayList<>(children);
            copy.add(new MapNode().retype("implements"));
            return copy;
        }).orElse(retyped);

        return Optional.of(withImplements);
    }
}