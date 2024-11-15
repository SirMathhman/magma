package magma.app.compile;

import magma.api.Tuple;
import magma.api.stream.Streams;
import magma.java.JavaList;
import magma.java.JavaOrderedMap;

import static magma.Assembler.STACK_POINTER;
import static magma.app.compile.lang.casm.Instructions.instruct;

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
        final var last = frames.findLast().orElse(new JavaOrderedMap<>());
        final var newLast = last.put(name, type);
        final var withLast = frames.setLast(newLast);
        final var copy = new State(withLast, frameIndex, definitionIndex);
        return copy.moveToIndex(last.size());
    }

    private Tuple<State, JavaList<Node>> moveToIndex(long definitionIndex) {
        final var state = new State(frames, 0, definitionIndex);

        final var delta = definitionIndex - this.definitionIndex;
        final JavaList<Node> instructions;
        if (delta == 0) {
            instructions = new JavaList<>();
        } else {
            final var instruction = delta > 0
                    ? instruct("addv", delta)
                    : instruct("subv", delta);

            instructions = new JavaList<Node>()
                    .addLast(instruct("ldd", STACK_POINTER))
                    .addLast(instruction)
                    .addLast(instruct("stod", STACK_POINTER));
        }

        return new Tuple<>(state, instructions);
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
