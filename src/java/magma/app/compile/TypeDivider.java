package magma.app.compile;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.api.stream.Streams;
import magma.app.compile.rule.Divider;
import magma.app.compile.rule.Input;
import magma.app.error.FormattedError;

import java.util.Arrays;

public class TypeDivider implements Divider {
    @Override
    public StringBuilder concat(StringBuilder buffer, String slice) {
        return buffer.append(", ").append(slice);
    }

    @Override
    public Result<List<Input>, FormattedError> divide(Input input) {
        return new Ok<>(Streams.from(Arrays.stream(input.getInput().split(", ")).toList())
                .map(Input::new)
                .collect(MutableJavaList.collector()));
    }
}
