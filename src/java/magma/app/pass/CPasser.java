package magma.app.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

public class CPasser implements Passer {
    @Override
    public Result<PassUnit<Node>, CompileError> afterPass(PassUnit<Node> unit) {
        return new Ok<>(unit);
    }
}
