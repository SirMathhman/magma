package magma;

import magma.api.collect.List;
import magma.api.collect.MutableList;
import magma.api.stream.Streams;
import magma.app.compile.rule.Splitter;

import java.util.Arrays;

public class TypeSplitter implements Splitter {
    @Override
    public StringBuilder merge(StringBuilder buffer, String slice) {
        return buffer.append(", ").append(slice);
    }

    @Override
    public List<String> split(String root) {
        return Streams.from(Arrays.stream(root.split(", ")).toList())
                .collect(MutableList.collector());
    }
}
