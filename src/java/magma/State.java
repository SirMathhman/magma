package magma;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

import static magma.Operation.StoreDirect;

final class State {
    private final Stack stack;
    private final List<Tuple<String, Label>> labels;

    State(Stack stack, List<Tuple<String, Label>> labels) {
        this.stack = stack;
        this.labels = labels;
    }

    public State(List<Tuple<String, Label>> labels) {
        this(new Stack(), labels);
    }

    private List<Instruction> assign(String name, Function<Stack, List<Instruction>> loader) {
        final var instructions = new ArrayList<>(loader.apply(stack));
        instructions.add(StoreDirect.of(new DataAddress(stack.resolveAddress(name))));
        return instructions;
    }

    public State enter() {
        return new State(stack.enter(), labels);
    }

    public State exit() {
        return new State(stack.exit(), labels);
    }

    public State define(String labelName, String variableName, long size, Function<Stack, List<Instruction>> loader) {
        var next = assign(labelName, variableName, loader);
        return new State(next.stack.define(variableName, size), next.labels);
    }

    public State assign(String labelName, String destinationName, Function<Stack, List<Instruction>> loader) {
        return updateLabel(labelName, label -> label.instruct(assign(destinationName, loader)));
    }

    private State updateLabel(String labelName, Function<Label, Label> mapper) {
        final var tuple = findLabelWithIndex(labelName).orElseThrow();
        final var index = tuple.left();
        final var oldLabel = tuple.right();

        final var newLabel = mapper.apply(oldLabel);
        final var copy = new ArrayList<>(labels);
        copy.set(index, new Tuple<>(labelName, newLabel));
        return new State(stack, copy);
    }

    private Optional<Tuple<Integer, Label>> findLabelWithIndex(String labelName) {
        return IntStream.range(0, labels.size())
                .mapToObj(index -> new Tuple<>(index, labels.get(index)))
                .filter(tuple -> tuple.right().left().equals(labelName))
                .map(tuple -> tuple.mapRight(Tuple::right))
                .findFirst();
    }

    public State define(String name, long size) {
        return new State(stack.define(name, size), labels);
    }

    public List<Instruction> instructions() {
        return labels.stream()
                .map(Tuple::right)
                .map(Label::instructions)
                .flatMap(Collection::stream)
                .toList();
    }
}
