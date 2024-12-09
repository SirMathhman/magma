package magma;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

import static magma.Operation.*;

public final class State {
    private final Stack stack;
    private final List<Tuple<String, Label>> labels;

    State(Stack stack, List<Tuple<String, Label>> labels) {
        this.stack = stack;
        this.labels = labels;
    }


    public State(List<Tuple<String, Label>> labels) {
        this(new Stack(), labels);
    }

    public State() {
        this(new ArrayList<>());
    }

    State label(String name, Function<LabelContext, LabelContext> mapper) {
        final var entered = enter();
        final var applied = mapper.apply(new LabelContext(name, entered));
        return applied.state().exit();
    }

    private List<Instruction> assignAtOffset(String name, Loader loader, int offset) {
        final var instructions = new ArrayList<>(loader.load(stack));
        instructions.add(StoreDirect.of(new DataAddress(stack.resolveDataAddress(name) + offset)));
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
        return instruct(labelName, assign(destinationName, offset, loaders));
    }

    public State instruct(String labelName, List<Instruction> instructions) {
        return updateLabel(labelName, label -> label.instruct(instructions));
    }

    private List<Instruction> assign(String destinationName, int offset, List<Loader> loaders) {
        var newInstructions = new ArrayList<Instruction>();
        for (int index = 0; index < loaders.size(); index++) {
            final var loader = loaders.get(index);
            newInstructions.addAll(assignAtOffset(destinationName, loader, index + offset));
        }

        return newInstructions;
    }

    private State updateLabel(String labelName, Function<Label, Label> mapper) {
        final var optional = findLabelWithIndex(labelName);
        final var copy = new ArrayList<>(labels);
        if (optional.isPresent()) {
            var tuple = optional.get();
            final var index = tuple.left();
            final var oldLabel = tuple.right();

            final var newLabel = mapper.apply(oldLabel);
            copy.set(index, new Tuple<>(labelName, newLabel));
        } else {
            copy.add(new Tuple<>(labelName, mapper.apply(new Label())));
        }

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

    public State jump(String sourceLabel, String destinationLabel) {
        return updateLabel(sourceLabel, label -> {
            final var instruction = JumpValue.of(new FunctionAddress(resolveFunctionAddress(destinationLabel)));
            return label.instruct(Collections.singletonList(instruction));
        });
    }

    private long resolveFunctionAddress(String destinationLabel) {
        var total = 0L;
        for (Tuple<String, Label> label : labels) {
            if (label.left().equals(destinationLabel)) {
                return total;
            } else {
                total += label.right().size();
            }
        }
        return total;
    }
}
