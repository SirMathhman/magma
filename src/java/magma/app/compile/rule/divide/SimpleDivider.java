package magma.app.compile.rule.divide;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleDivider implements Divider {

    private final String delimiter;

    public SimpleDivider(String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    public String merge(String current, String value) {
        return current + this.delimiter + value;
    }

    @Override
    public Result<List<String>, CompileError> divide(String input) {
        return new Ok<>(Arrays.stream(input.split(Pattern.quote(this.delimiter))).toList());
    }
}