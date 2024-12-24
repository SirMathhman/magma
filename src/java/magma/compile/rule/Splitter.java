package magma.compile.rule;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.compile.error.CompileError;

public interface Splitter {
    Result<Tuple<String, String>, CompileError> split(String input);

    String merge(String left, String right);
}
