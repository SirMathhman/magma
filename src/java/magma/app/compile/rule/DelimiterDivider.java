package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.FormattedError;

public record DelimiterDivider(String delimiter) implements Divider {
    @Override
    public Result<List<Input>, FormattedError> divide(Input input) {
        List<Input> parts = new MutableJavaList<>();
        int start = 0;
        int index;

        while ((index = input.getInput().indexOf(delimiter, start)) != -1) {
            parts = parts.add(new Input(input.getInput().substring(start, index))); // Add substring before the delimiter
            start = index + delimiter.length();     // Move past the delimiter
        }

        // Add the remaining part of the string
        parts = parts.add(new Input(input.getInput().substring(start)));
        return new Ok<>(parts); // Wrap the result in magma.api.collect.List
    }

    @Override
    public StringBuilder concat(StringBuilder buffer, String slice) {
        return buffer.append(delimiter).append(slice);
    }
}
