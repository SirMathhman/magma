package magma.app.rule.divide;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.Arrays;
import java.util.List;

public class SimpleDivider implements Divider {
    @Override
    public String merge(String current, String value) {
        return current + " " + value;
    }

    @Override
    public Result<List<String>, CompileError> divide(String input) {
        return new Ok<>(Arrays.stream(input.split(" ")).toList());
    }
}
