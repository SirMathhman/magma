package magma.compile.rule.slice;

import magma.api.result.Result;
import magma.compile.error.CompileError;

import java.util.List;

public interface Slicer {
    StringBuilder merge(StringBuilder builder, String value);

    Result<List<String>, CompileError> slice(String root);
}
