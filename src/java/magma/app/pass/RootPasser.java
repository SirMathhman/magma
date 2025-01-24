package magma.app.pass;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

public class RootPasser implements Passer {
    @Override
    public Result<PassUnit<Node>, CompileError> beforePass(PassUnit<Node> unit) {
        return new Ok<>(unit.filterAndMapToValue(Passer.by("class").or(Passer.by("record")).or(Passer.by("interface")), node -> node.retype("struct"))
                .or(() -> unit.filterAndMapToValue(Passer.by("import"), node -> node.retype("include")))
                .orElse(unit));
    }
}