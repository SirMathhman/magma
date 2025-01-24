package magma.app;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.ArrayList;

import static magma.app.lang.CommonLang.CONTENT_CHILDREN;

public class RootPasser implements Passer {
    private static Node getStruct(Node node) {
        return node.retype("struct").mapNode("value", value -> {
            return value.mapNodeList(CONTENT_CHILDREN, children -> {
                final var copy = new ArrayList<Node>(children);
                final var impl = new MapNode("struct")
                        .withString("name", "Impl")
                        .withNode("value", new MapNode("block"));

                copy.add(impl);
                return copy;
            });
        });
    }

    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(
                unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")), node -> node.retype("struct"))
                        .or(() -> unit.filterAndMapToValue(Passer.by("interface"), RootPasser::getStruct)).orElse(unit));
    }
}