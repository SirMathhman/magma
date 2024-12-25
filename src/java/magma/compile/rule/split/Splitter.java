package magma.compile.rule.split;

import magma.api.Tuple;
import magma.api.result.Result;
import magma.compile.error.CompileError;

import java.util.List;

public interface Splitter {
    StringBuilder merge(StringBuilder builder, String value);

    Result<List<String>, CompileError> split(String root);
}
