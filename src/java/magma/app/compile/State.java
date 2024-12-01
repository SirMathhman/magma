package magma.app.compile;

import magma.java.JavaNonEmptyList;
import magma.java.JavaOrderedMap;

public record State(JavaNonEmptyList<JavaOrderedMap<String, Node>> frames) {
    public State() {
        this(new JavaNonEmptyList<>(new JavaOrderedMap<>()));
    }

    public State define(String name, Node type) {
        return new State(frames.mapLast(last -> last.put(name, type)));
    }
}
