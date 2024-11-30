package magma.app.compile.lang.magma;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.PassingStage;

import static magma.app.compile.pass.Starter.START_LABEL;

public class FunctionWrapper implements PassingStage {
    @Override
    public Result<Node, CompileError> pass(Node root) {
        return new Ok<>(new MapNode("function")
                .withString("name", START_LABEL)
                .withNode("value", root.retype("block")));
    }
}
