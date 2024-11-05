package magma.app.compile;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.app.compile.error.StringContext;
import magma.app.compile.lang.CommonLang;

public class SymbolPasser implements Passer {
    private static Result<String, CompileError> passSymbolTypeValue(String value) {
        if (value.equals("I32")) return new Ok<>("int");
        else return new Err<>(new CompileError("Unknown value", new StringContext(value)));
    }

    @Override
    public Result<Node, CompileError> pass(Node type) {
        if (!type.is(CommonLang.SYMBOL_TYPE)) return new Ok<>(type);

        return type.mapString(CommonLang.SYMBOL_VALUE, SymbolPasser::passSymbolTypeValue)
                .orElse(new Ok<>(type));
    }
}