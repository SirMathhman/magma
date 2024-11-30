package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.java.JavaList;
import magma.java.JavaNonEmptyList;

import static magma.Compiler.SPILL;
import static magma.Compiler.STACK_POINTER;
import static magma.app.assemble.Operator.*;
import static magma.app.compile.CASMLang.instruct;

public record Stack(
        JavaNonEmptyList<JavaOrderedMap<String, Layout>> frames,
        int frameIndex,
        int elementIndex,
        JavaList<Integer> indices
) {
    public Stack() {
        this(new JavaNonEmptyList<>(new JavaOrderedMap<>()), 0, 0, new JavaList<>());
    }

    private static Option<Tuple<Integer, Integer>> pairWithIndex(Tuple<Integer, JavaOrderedMap<String, Layout>> indexAndFrame, String name) {
        final var frameIndex = indexAndFrame.left();
        final var map = indexAndFrame.right();
        return map.findKeyIndex(name).map(indexWithinFrame -> new Tuple<>(frameIndex, indexWithinFrame));
    }

    private static int computeFrameSize(JavaOrderedMap<String, Layout> frame) {
        return computeFrameSliceSize(frame.stream());
    }

    private static int computeFrameSliceSize(Stream<Tuple<String, Layout>> elements) {
        return elements.map(Tuple::right)
                .map(Layout::computeElementSizeTo)
                .foldLeft(0, Integer::sum);
    }

    private static JavaList<Node> createMoveInstructions(Node instruction) {
        return new JavaList<Node>()
                .add(instruct(StoreDirectly, SPILL))
                .add(instruct(LoadDirectly, STACK_POINTER))
                .add(instruction)
                .add(instruct(StoreDirectly, STACK_POINTER))
                .add(instruct(LoadDirectly, SPILL));
    }

    public Option<Tuple<Stack, JavaList<Node>>> moveTo(String name) {
        return moveTo(name, new JavaList<>());
    }

    private int computeFrameSize(int frameIndexExclusive) {
        return frames.sliceTo(frameIndexExclusive)
                .map(slice -> slice.map(Stack::computeFrameSize).foldLeft(0, Integer::sum))
                .orElse(0);
    }

    private int computePosition(int frameIndex, int layoutIndex, JavaList<Integer> indices) {
        final var beforeFrameSize = computeFrameSize(frameIndex);

        return frames.get(frameIndex).flatMap(frame -> {
            final var beforeElementsSize = frame.sliceTo(layoutIndex)
                    .map(Stack::computeFrameSliceSize)
                    .orElse(0);

            return frame.getValue(layoutIndex).map(layout -> {
                final var size = layout.computeElementSizeTo(indices).orElse(0);
                return beforeFrameSize + beforeElementsSize + size;
            });
        }).orElse(0);
    }

    private Tuple<Stack, JavaList<Node>> moveTo(int frameIndex, int elementIndex, JavaList<Integer> indices) {
        final var fromPosition = computePosition();
        final var toPosition = computePosition(frameIndex, elementIndex, indices);
        if (fromPosition == toPosition) return new Tuple<>(this, new JavaList<>());

        var copy = new Stack(frames, frameIndex, elementIndex, indices);
        final var delta = toPosition - fromPosition;
        final var distance = Math.abs(delta);
        final var shouldMoveRight = distance > 0;
        final var instructions = shouldMoveRight
                ? instruct(AddFromValue, distance)
                : instruct(SubtractFromValue, distance);

        return new Tuple<>(copy, createMoveInstructions(instructions));
    }

    private int computePosition() {
        return computePosition(frameIndex, elementIndex, indices);
    }

    public Stack define(String name, Layout type) {
        return new Stack(frames.mapLast(last -> last.put(name, type)), frameIndex, elementIndex, indices);
    }

    public Option<Tuple<Stack, JavaList<Node>>> moveTo(String name, JavaList<Integer> indices) {
        return frames.streamReverseWithIndices()
                .map(frame -> pairWithIndex(frame, name))
                .flatMap(Streams::fromOption)
                .next()
                .map(foundFrame -> {
                    final var frameIndex = foundFrame.left();
                    final var elementIndex = foundFrame.right();
                    return moveTo(frameIndex, elementIndex, indices);
                });
    }

    public Option<Layout> resolve(String name) {
        return frames.stream()
                .flatMap(JavaOrderedMap::stream)
                .filter(tuple -> tuple.left().equals(name))
                .map(Tuple::right)
                .next();
    }
}
