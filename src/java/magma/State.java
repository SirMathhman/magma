package magma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
        this(new Stack(), new Label(new ArrayList<>()));
    }

    public State enter() {
        return new State(stack.enter(), new Label(label.instructions()));
    }

    public State exit() {
        return new State(stack.exit(), new Label(label.instructions()));
    }

    public State defineData(String name, long size, Function<Stack, List<Instruction>> loader) {
        final var withA = stack.define(name, size);
        final var instructions = assign(name, loader);
        return new State(withA, new Label(instructions));
    }

    public State assignAsState(String name, Function<Stack, List<Instruction>> loader) {
        final var instructions = assign(name, loader);
        return new State(stack, new Label(instructions));
    }

    private List<Instruction> assign(String name, Function<Stack, List<Instruction>> loader) {
        final var instructions = Stream.of(loader.apply(stack), List.of(StoreDirect.of(new DataAddress(stack.resolveAddress(name)))))
                .flatMap(Collection::stream)
                .toList();

        final var copy = new ArrayList<>(label.instructions());
        copy.addAll(instructions);
        return copy;
    }

    public State defineData(String name, long size) {
        return new State(stack.define(name, size), new Label(label.instructions()));
    }

    public Stack stack() {
        return stack;
    }

    public List<Instruction> instructions() {
        return label.instructions();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (State) obj;
        return Objects.equals(this.stack, that.stack) && Objects.equals(label.instructions(), that.label.instructions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, label.instructions());
    }

    @Override
    public String toString() {
        return "State[" +
                "stack=" + stack + ", " +
                "instructions=" + label.instructions() + ']';
    }
}
