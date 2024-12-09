package magma;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static magma.Operation.StoreDirect;

final class State {
    private final Stack stack;
    private final Label label;

    State(Stack stack, Label label) {
        this.stack = stack;
        this.label = label;
    }

    public State() {
        this(new Stack(), new Label());
    }

    public State enter() {
        return new State(stack.enter(), label);
    }

    public State exit() {
        return new State(stack.exit(), label);
    }

    public State defineData(String name, long size, Function<Stack, List<Instruction>> loader) {
        final var withA = stack.define(name, size);
        final var label1 = label.instruct(createAssignmentInstructions(name, loader));
        return new State(withA, label1);
    }

    private List<Instruction> createAssignmentInstructions(String name, Function<Stack, List<Instruction>> loader) {
        return Stream.of(loader.apply(stack), List.of(StoreDirect.of(new DataAddress(stack.resolveAddress(name)))))
                .flatMap(Collection::stream)
                .toList();
    }

    public State assignAsState(String name, Function<Stack, List<Instruction>> loader) {
        final var label1 = label.instruct(createAssignmentInstructions(name, loader));
        return new State(stack, label1);
    }

    public State defineData(String name, long size) {
        return new State(stack.define(name, size), label);
    }

    public List<Instruction> instructions() {
        return label.instructions();
    }
}
