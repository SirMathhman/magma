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
    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), node -> {
            return node.retype("struct").mapNode("value", value -> {
                return value.mapNodeList("children", children -> {
                    final var methods = new ArrayList<Node>();
                    final var others = new ArrayList<Node>();

                    children.forEach(child -> {
                        if (child.is("method")) {
                            methods.add(child);
                        } else {
                            others.add(child);
                        }
                    });

                    return List.of(
                            new MapNode("struct")
                                    .withString("name", "Table")
                                    .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, methods)),
                            new MapNode("struct")
                                    .withString("name", "Impl")
                                    .withNode("value", new MapNode("block").withNodeList(CONTENT_CHILDREN, others))
                    );
                });
            });
        }).orElse(unit));
    }
}
