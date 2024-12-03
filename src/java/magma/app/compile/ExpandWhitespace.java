package magma.app.compile;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.magma.Stateless;

public class ExpandWhitespace implements Stateless {
    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        return new Ok<>(CommonLang.createEmptyGroup());
    }
}
