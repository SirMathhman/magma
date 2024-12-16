package magma;

import magma.error.CompileError;
import magma.result.Result;

public interface Rule {
    Result<String, CompileError> generate(String value);
}
