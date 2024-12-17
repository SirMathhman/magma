package magma.app.compile;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;
import magma.api.stream.Streams;
import magma.app.compile.rule.Divider;

import java.util.Arrays;

public class TypeDivider implements Divider {
    @Override
    public StringBuilder concat(StringBuilder buffer, String slice) {
        return buffer.append(", ").append(slice);
    }

    @Override
    public List<String> divide(String root) {
        return Streams.from(Arrays.stream(root.split(", ")).toList())
                .collect(MutableJavaList.collector());
    }
}
