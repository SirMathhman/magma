package magma.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(Collections.emptyList());
    }

    public JavaList<T> add(T element) {
        var copy = new ArrayList<>(list());
        copy.add(element);
        return new JavaList<T>(copy);
    }
}