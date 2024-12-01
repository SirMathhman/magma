package magma.app.compile;

import magma.api.option.Option;
import magma.api.stream.Streams;
import magma.java.JavaNonEmptyList;
import magma.java.JavaOrderedMap;

public record State(JavaNonEmptyList<JavaOrderedMap<String, Node>> frames) {
    public State() {
        this(new JavaNonEmptyList<>(new JavaOrderedMap<>()));
    }

    public State define(String name, Node type) {
        return new State(frames.mapLast(last -> last.put(name, type)));
    }

    public Option<Node> lookup(String name) {
        return frames.streamReverse()
                .map(frame -> frame.find(name))
                .flatMap(Streams::fromOption)
                .next();
    }
}
