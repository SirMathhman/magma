package magma.app.compile.lang.casm;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.casm.assemble.Operator;
import magma.app.compile.lang.magma.Stateless;

import static magma.app.compile.lang.casm.CASMLang.instruct;

public class ResolveNumericValue implements Stateless {
    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        final var intValue = node.findInt("value").orElse(0);
        var result = instruct(Operator.LoadFromValue, intValue);
        return new Ok<>(result);
    }
}
