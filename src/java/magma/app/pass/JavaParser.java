package magma.app.pass;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;

public class JavaParser implements Passer {
    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        final var value = unit.value();
        if (value.is("symbol")) {
            return new Err<>(new CompileError("Symbol not defined", new NodeContext(value)));
        }

        return new Ok<>(unit);
    }
}
