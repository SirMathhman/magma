import magma.collect.List;
import magma.java.JavaList;
struct Splitter {static void advance(JavaList<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }StringBuilder merge(StringBuilder inner, String compiled);List<String> split(String input);
}