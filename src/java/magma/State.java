package magma;

import java.util.*;
import java.util.function.Function;

import static magma.Operation.*;

public final class State {
    private final Stack stack;
    private final Labels labels;

    State(Stack stack, Labels labels) {
        this.stack = stack;
        this.labels = labels;
    }

    public State() {
        this(new Stack(), new Labels());
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
        final var labels1 = labels.updateLabel(labelName, label -> label.instruct(instructions));
        return new State(stack, labels1);
    }

    private List<Instruction> assign(String destinationName, int offset, List<Loader> loaders) {
        var newInstructions = new ArrayList<Instruction>();
        for (int index = 0; index < loaders.size(); index++) {
            final var loader = loaders.get(index);
            newInstructions.addAll(assignAtOffset(destinationName, loader, index + offset));
        }

        return newInstructions;
    }

    public State define(String name, long size) {
        return new State(stack.define(name, size), labels);
    }

    public List<Instruction> instructions() {
        return labels.flatten();
    }

    public State jump(String sourceLabel, String destinationLabel) {
        final var labels1 = labels.updateLabel(sourceLabel, label -> {
            final var address = labels.resolveFunctionAddress(destinationLabel).orElseThrow();
            final var instruction = JumpValue.of(new FunctionAddress(address));
            return label.instruct(Collections.singletonList(instruction));
        });

        return new State(stack, labels1);
    }
}
