package magma.compile.rule.split;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.error.CompileError;

import java.util.Arrays;
import java.util.List;

public class TypeSplitter implements Splitter {
    @Override
    public StringBuilder merge(StringBuilder builder, String value) {
        return builder.append(", ").append(value);
    }

    @Override
    public Result<List<String>, CompileError> split(String root) {
        return new Ok<>(Arrays.stream(root.split(",")).toList());
    }
}
