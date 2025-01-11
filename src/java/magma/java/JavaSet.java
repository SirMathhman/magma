package magma.java;

import java.util.Set;
import java.util.stream.Stream;

public record JavaSet<T>(Set<T> sources) {
    public Stream<T> stream() {
        return sources().stream();
    }
}