package magma.app.compile.lang.magma;

import magma.api.Tuple;
import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.State;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.NodeContext;
import magma.app.compile.pass.PassingStage;

public class WrapRoot implements PassingStage {
    @Override
    public Result<Tuple<State, Node>, CompileError> pass(State state, Node root) {
        if (!root.is("root"))
            return new Err<>(new CompileError("Not a root node", new NodeContext(root)));

        final var block = root.retype("block");
        return new Ok<>(new Tuple<>(state, new MapNode("function")
                .withString("name", "__start__")
                .withNode("value", block)));
    }
}
