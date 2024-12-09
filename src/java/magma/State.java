package magma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static magma.Operation.StoreDirect;

record State(Stack stack, List<Instruction> instructions) {
    public State() {
        this(new Stack(), new ArrayList<>());
    }

    public State enter() {
        return new State(stack.enter(), instructions);
    }

    public State exit() {
        return new State(stack.exit(), instructions);
    }

    public State defineData(String name, long size, Function<Stack, List<Instruction>> loader) {
        final var withA = stack.define(name, size);
        final var instructions = assign(name, loader);
        return new State(withA, instructions);
    }

    public State assignAsState(String name, Function<Stack, List<Instruction>> loader) {
        final var instructions = assign(name, loader);
        return new State(stack, instructions);
    }

    private List<Instruction> assign(String name, Function<Stack, List<Instruction>> loader) {
        final var instructions = Stream.of(loader.apply(stack), List.of(StoreDirect.of(new DataAddress(stack.resolveAddress(name)))))
                .flatMap(Collection::stream)
                .toList();

        final var copy = new ArrayList<>(this.instructions);
        copy.addAll(instructions);
        return copy;
    }

    public State defineData(String name, long size) {
        return new State(stack.define(name, size), instructions);
    }
}
