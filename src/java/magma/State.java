package magma;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static magma.Operation.StoreDirect;

final class State {
    private final Stack stack;
    private final Map<String, Label> labels;

    State(Stack stack, Map<String, Label> labels) {
        this.stack = stack;
        this.labels = labels;
    }

    public State() {
        this(new Stack(), Map.of("main", new Label()));
    }

    public State enter() {
        return new State(stack.enter(), labels);
    }

    public State exit() {
        return new State(stack.exit(), labels);
    }

    public State defineData(String name, long size, Function<Stack, List<Instruction>> loader) {
        final var defined = stack.define(name, size);
        final var main = updateLabel("main", label -> label.instruct(createAssignmentInstructions(name, loader)));
        return new State(defined, main);
    }

    private List<Instruction> createAssignmentInstructions(String name, Function<Stack, List<Instruction>> loader) {
        return Stream.of(loader.apply(stack), List.of(StoreDirect.of(new DataAddress(stack.resolveAddress(name)))))
                .flatMap(Collection::stream)
                .toList();
    }

    public State assignAsState(String name, Function<Stack, List<Instruction>> loader) {
        final var main = updateLabel("main", label -> label.instruct(createAssignmentInstructions(name, loader)));
        return new State(stack, main);
    }

    private Map<String, Label> updateLabel(String labelName, Function<Label, Label> mapper) {
        final var oldLabel = labels.get(labelName);
        final var newLabel = mapper.apply(oldLabel);
        return Map.of(labelName, newLabel);
    }

    public State defineData(String name, long size) {
        return new State(stack.define(name, size), labels);
    }

    public List<Instruction> instructions() {
        return labels.get("main").instructions();
    }
}
