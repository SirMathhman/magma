package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Streams;
import magma.java.JavaList;
import magma.java.JavaOrderedMap;

public record State(JavaList<JavaOrderedMap<String, Node>> frames, long frameIndex, long definitionIndex) {
    public State() {
        this(new JavaList<JavaOrderedMap<String, Node>>().addLast(new JavaOrderedMap<>()), 0, 0);
    }

    public State enter() {
        return new State(frames.addLast(new JavaOrderedMap<>()), frameIndex, definitionIndex);
    }

    public State exit() {
        return new State(frames.popLastAndDrop().orElse(frames), frameIndex, definitionIndex);
    }

    public Tuple<State, JavaList<Node>> loadLabel(String label) {
        return new Tuple<>(this, new JavaList<>());
    }

    public Tuple<State, JavaList<Node>> define(String name, Node type) {
        final var copy = frames.mapLast(last -> last.put(name, type)).orElse(frames);
        return moveToIndex(0, 0);
    }

    private Tuple<State, JavaList<Node>> moveToIndex(long frameIndex, long definitionIndex) {
        return new Tuple<>(this, new JavaList<Node>());
    }

    private int computeFrameSize(JavaOrderedMap<String, Node> frame) {
        return frame.stream()
                .map(Tuple::right)
                .map(node -> node.findInt("length"))
                .flatMap(Streams::fromOption)
                .foldLeft(0, Integer::sum);
    }

    public int depth() {
        return Math.max(frames.size() - 1, 0);
    }
}
