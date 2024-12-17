package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.java.MutableJavaList;

public record DelimiterSplitter(String delimiter) implements Splitter {
    @Override
    public List<String> split(String root) {
        List<String> parts = new MutableJavaList<>();
        int start = 0;
        int index;

        while ((index = root.indexOf(delimiter, start)) != -1) {
            parts = parts.add(root.substring(start, index)); // Add substring before the delimiter
            start = index + delimiter.length();     // Move past the delimiter
        }

        // Add the remaining part of the string
        parts = parts.add(root.substring(start));
        return parts; // Wrap the result in magma.api.collect.List
    }

    @Override
    public StringBuilder merge(StringBuilder buffer, String slice) {
        return buffer.append(delimiter).append(slice);
    }
}
