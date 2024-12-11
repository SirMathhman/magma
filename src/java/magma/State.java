package magma;

import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class State {
    private final Deque<Integer> input;
    private final List<Integer> memory;
    private int programCounter;
    private int accumulator = 0;

    public State(Deque<Integer> input, List<Integer> memory, int programCounter) {
        this.input = input;
        this.memory = memory;
        this.programCounter = programCounter;
    }

    String display() {
        return IntStream.range(0, memory.size())
                .mapToObj(index -> new Tuple<>(index, memory.get(index)))
                .map(tuple -> tuple.mapRight(Instruction::decode))
                .map(tuple -> tuple.mapRight(Instruction::display))
                .map(tuple -> tuple.mapLeft(Integer::toHexString))
                .map(tuple -> tuple.left() + ") " + tuple.right())
                .collect(Collectors.joining("\n"));
    }

    State inAndStore(int addressOrValue) {
        final var first = input.removeFirst();
        return set(addressOrValue, first);
    }

    private State set(int address, int value) {
        while (!(address < memory.size())) {
            memory.add(0);
        }

        memory.set(address, value);
        return this;
    }

    State next() {
        this.programCounter = programCounter + 1;
        return this;
    }

    public Optional<Integer> current() {
        if (programCounter >= this.memory.size()) return Optional.empty();
        return Optional.of(this.memory.get(programCounter));
    }

    public State jump(int addressOrValue) {
        programCounter = addressOrValue;
        return this;
    }

    public State loadValue(int value) {
        accumulator = value;
        return this;
    }

    public State storeIndirect(int address) {
        return storeDirect(memory.get(address));
    }

    State storeDirect(int address) {
        return set(address, accumulator);
    }

    public State loadDirect(int address) {
        return loadValue(memory.get(address));
    }

    public State addValue(int addressOrValue) {
        accumulator += addressOrValue;
        return this;
    }

    public State subtractValue(int addressOrValue) {
        accumulator -= addressOrValue;
        return this;
    }
}
