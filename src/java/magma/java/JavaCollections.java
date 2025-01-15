package magma.java;

import java.util.ArrayList;
import java.util.List;

public class JavaCollections {
    public static <T> List<T> foldList(List<T> list, T element) {
        final var copy = new ArrayList<>(list);
        copy.add(element);
        return copy;
    }
}
