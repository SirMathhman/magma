package magma.compile.rule.split;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.compile.error.CompileError;

import java.util.ArrayList;
import java.util.List;

public class TypeSplitter implements Splitter {
    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    @Override
    public StringBuilder merge(StringBuilder builder, String value) {
        return builder.append(", ").append(value);
    }

    @Override
    public Result<List<String>, CompileError> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            if (c == ',' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
                if (c == '<') depth++;
                if (c == '>') depth--;
            }
        }
        advance(buffer, segments);

        return new Ok<>(segments);
    }
}
