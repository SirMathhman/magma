package magma.app.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;

import java.util.ArrayList;

import static magma.app.pass.RootPasser.createAnyRefType;

public class CPasser implements Passer {
    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("method"), node -> {
            final var oldParams = node.findNodeList("params").orElse(new ArrayList<>());
            final var copy = new ArrayList<Node>();
            copy.add(new MapNode("definition")
                    .withNode("type", createAnyRefType())
                    .withString("name", "_ref_"));
            copy.addAll(oldParams);
            return node.withNodeList("params", copy);
        }).orElse(unit));
    }
}
