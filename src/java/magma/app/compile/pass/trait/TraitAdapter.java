package magma.app.compile.pass.trait;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.pass.Passer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.*;

public class TraitAdapter implements Passer  {
    @Override
    public Optional<Node> beforePass(Node node) {
        return Optional.of(node.mapNodeList(TYPE_PARAMS, params -> {
            var copy = new ArrayList<Node>();
            copy.add(createTypeParameter());
            copy.addAll(params);
            return copy;
        }).orElseGet(() -> node.withNodeList(TYPE_PARAMS, List.of(createTypeParameter()))));
    }

    private static Node createTypeParameter() {
        return new MapNode().retype(SYMBOL_TYPE).withNodeList(NAMESPACE, List.of(new MapNode().retype(NAMESPACE_SEGMENT_TYPE).withString(NAMESPACE_SEGMENT_VALUE, "Self")));
    }
}
