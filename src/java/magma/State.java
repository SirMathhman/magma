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

    private List<List<Instruction>> assign(String name, List<Loader> loaders) {
        return assign(name, 0, loaders);
    }

    private List<List<Instruction>> assign(String name, int offset, List<Loader> loaders) {
        var newInstructions = new ArrayList<List<Instruction>>();
        for (int index = 0; index < loaders.size(); index++) {
            final var loader = loaders.get(index);
            newInstructions.add(assignAtOffset(name, loader, index + offset));
        }
        return newInstructions;
    }

    private List<Instruction> assignAtOffset(String name, Loader loader, int offset) {
        final var instructions = new ArrayList<>(loader.load(stack));
        instructions.add(StoreDirect.of(new DataAddress(stack.resolveAddress(name) + offset)));
        return instructions;
    }

    public State enter() {
        return new State(stack.enter(), labels);
    }

    public State exit() {
        return new State(stack.exit(), labels);
    }

    public State define(String labelName, String variableName, List<Loader> loaders) {
        return assign(labelName, variableName, 0, loaders).define(variableName, loaders.size());
    }

    public State assign(String labelName, String destinationName, int offset, List<Loader> loaders) {
        return updateLabel(labelName, label -> {
            return assign(destinationName, offset, loaders)
                    .stream()
                    .reduce(label, Label::instruct, (_, next) -> next);
        });
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
