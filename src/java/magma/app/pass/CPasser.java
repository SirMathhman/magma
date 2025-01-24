package magma.app.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.MapNode;
import magma.app.Node;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;

import static magma.app.lang.CommonLang.CONTENT_CHILDREN;

public class CPasser implements Passer {
    private static List<Node> getNodes(List<Node> children) {
        final var methods = new ArrayList<Node>();
        final var others = new ArrayList<Node>();

        children.forEach(child -> {
            if (child.is("method")) {
                methods.add(child);
            } else {
                others.add(child);
            }
        });

        final var tableDef = new MapNode("struct")
                .withString("name", "Table")
                .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, methods));

        final var implDef = new MapNode("struct")
                .withString("name", "Impl")
                .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, others));

        return List.of(
                tableDef,
                implDef,
                new MapNode("definition")
                        .withString("name", "table")
                        .withNode("type", new MapNode("struct").withString("value", "Table")),
                new MapNode("definition")
                        .withString("name", "impl")
                        .withNode("type", new MapNode("struct").withString("value", "Impl"))
        );
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), node -> {
            return node.retype("struct").mapNode("value", value -> {
                return value.mapNodeList("children", CPasser::getNodes);
            });
        }).orElse(unit));
    }
}
