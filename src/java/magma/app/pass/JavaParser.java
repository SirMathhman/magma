package magma.app.pass;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;

import java.util.List;

public class JavaParser implements Passer {

    public static final List<String> PRIMITIVES = List.of("String", "int");

    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        final var node = unit.value();
        if (node.is("symbol")) {
            final var value = node.findString("value").orElse("");
            if (!PRIMITIVES.contains(value)) {
                return new Err<>(new CompileError("Symbol not defined", new NodeContext(node)));
            }
        }

        return new Ok<>(unit);
    }
}
