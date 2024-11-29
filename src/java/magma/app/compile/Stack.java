package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.stream.Stream;
import magma.api.stream.Streams;
import magma.java.JavaList;
import magma.java.JavaNonEmptyList;

import static magma.Main.SPILL;
import static magma.Main.STACK_POINTER;
import static magma.app.assemble.Operator.*;
import static magma.app.compile.CASMLang.instruct;

public record Stack(
        JavaNonEmptyList<JavaOrderedMap<String, Layout>> frames,
        int frameIndex,
        Option<Integer> elementIndex
) {
    public Stack() {
        this(new JavaNonEmptyList<>(new JavaOrderedMap<>()), 0, new None<>());
    }

    private static Option<Tuple<Integer, Integer>> pairWithIndex(Tuple<Integer, JavaOrderedMap<String, Layout>> indexAndFrame, String name) {
        return indexAndFrame.right()
                .findKeyIndex(name)
                .map(index -> new Tuple<>(indexAndFrame.left(), index));
    }

    private static int computeFrameSize(JavaOrderedMap<String, Layout> frame) {
        return computeFrameSliceSize(frame.stream());
    }

    private static int computeFrameSliceSize(Stream<Tuple<String, Layout>> elements) {
        return elements.map(Tuple::right)
                .map(Layout::computeSize)
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
        return frames.streamReverseWithIndices()
                .map(frame -> pairWithIndex(frame, name))
                .flatMap(Streams::fromOption)
                .next()
                .map(foundFrame -> moveTo(foundFrame.left(), foundFrame.right()));
    }

    private int computeFrameSize(int frameIndexExclusive) {
        return frames.sliceTo(frameIndexExclusive)
                .map(slice -> slice.map(Stack::computeFrameSize).foldLeft(0, Integer::sum))
                .orElse(0);
    }

    private int computePosition(int frameIndex, int layoutIndex) {
        final var beforeFrameSize = computeFrameSize(frameIndex);

        return frames.get(frameIndex).map(frame -> {
            final var beforeElementsSize = frame.sliceTo(layoutIndex)
                    .map(Stack::computeFrameSliceSize)
                    .orElse(0);

            return beforeFrameSize + beforeElementsSize;
        }).orElse(0);
    }

    private Tuple<Stack, JavaList<Node>> moveTo(int frameIndex, int elementIndex) {
        final var fromPosition = computePosition();
        final var toPosition = computePosition(frameIndex, elementIndex);
        if (fromPosition == toPosition) return new Tuple<>(this, new JavaList<>());

        var copy = new Stack(frames, frameIndex, new Some<>(elementIndex));
        final var delta = toPosition - fromPosition;
        final var distance = Math.abs(delta);
        final var shouldMoveRight = distance > 0;
        final var instructions = shouldMoveRight
                ? instruct(AddFromValue, distance)
                : instruct(SubtractFromValue, distance);

        return new Tuple<>(copy, createMoveInstructions(instructions));
    }

    private int computePosition() {
        return computePosition(frameIndex, elementIndex.orElse(0));
    }

    public Stack define(String name, Layout type) {
        return new Stack(frames.mapLast(last -> last.put(name, type)), frameIndex, elementIndex);
    }

    public interface Layout {
        int computeSize();
    }
}
