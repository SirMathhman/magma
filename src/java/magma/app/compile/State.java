package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;
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

    public Option<Node> resolve(String name) {
        return resolveIndexAndType(name)
                .map(Tuple::right)
                .map(Tuple::right);
    }

    private Option<Tuple<Integer, Tuple<Integer, Node>>> resolveIndexAndType(String name) {
        return frames.streamReverseWithIndices()
                .map(frame -> frame.right().findIndexAndValue(name).map(inner -> new Tuple<>(frame.left(), inner)))
                .flatMap(Streams::fromOption)
                .next();
    }

    public Option<Integer> computeDistance(String name) {
        return resolveIndexAndType(name).map(tuple -> {
            final var other = tuple.right();

            final var requestedFrameIndex = tuple.left();
            final var requestedElementIndex = other.left();

            final var currentFrameIndex = frames.size() - 1;
            final var currentElementIndex = frames.last().size() - 1;

            if (requestedFrameIndex == currentFrameIndex &&
                    requestedElementIndex == currentElementIndex) return 0;

            else {
                throw new UnsupportedOperationException();
            }
        });
    }

    private int computeSizeFromFrameIndex(int frameIndex) {
        return frames.sliceFrom(frameIndex)
                .map(slice -> slice.map(frame -> computeFrameSize(frame.stream())).foldLeft(0, Integer::sum))
                .orElse(0);
    }

    private int computeFrameSize(Stream<Tuple<String, Node>> frameStream) {
        return frameStream.map(Tuple::right)
                .map(right -> right.findInt("length").orElse(0))
                .foldLeft(0, Integer::sum);
    }
}
