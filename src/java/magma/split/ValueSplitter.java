package magma.split;

import magma.collect.List;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.Strings;

public class ValueSplitter implements Splitter {
    @Override
    public StringBuilder merge(StringBuilder inner, String compiled) {
        return inner.append(", ").append(compiled);
    }

    @Override
    public List<String> split(String input) {
        final var inputParamsJavaList = new JavaList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = Strings.streamChars(input).collect(JavaLinkedList.collector());

        while (!queue.isEmpty()) {
            final var c = queue.popOrPanic();
            if (c == ',' && depth == 0) {
                Splitter.advance(inputParamsJavaList, buffer);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
                if (c == '-') {
                    if (!queue.isEmpty() && queue.peek().filter(value -> value == '>').isPresent()) {
                        buffer.append(queue.popOrPanic());
                    }
                }
                if (c == '<' || c == '(') depth++;
                if (c == '>' || c == ')') depth--;
            }
        }
        Splitter.advance(inputParamsJavaList, buffer);
        return inputParamsJavaList;
    }
}
