package magma;

import java.util.ArrayList;
import java.util.List;

public record JavaList<T>(List<T> list) {
    public JavaList() {
        this(new ArrayList<>());
    }

    public JavaList<T> add(T value) {
        list.add(value);
        return this;
    }

    public JavaList<T> addAll(JavaList<T> other) {
        list.addAll(other.list);
        return this;
    }
}
