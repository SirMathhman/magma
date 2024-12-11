package magma;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class State {
    public static final String DISPLAY_FORMAT = """
            Memory:
            %s
            
            Program Counter: %s
            Accumulator: %s""";
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
        return DISPLAY_FORMAT.formatted(Instruction.displayEncoded(memory), Integer.toHexString(programCounter), Integer.toHexString(accumulator));
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

    public Result<State, RuntimeError> loadDirect(int address) {
        if (address >= 0 && address < memory.size()) {
            return new Ok<>(loadValue(memory.get(address)));
        }
        return new Err<>(new RuntimeError("Invalid address", String.valueOf(address)));
    }

    public State addValue(int addressOrValue) {
        accumulator += addressOrValue;
        return this;
    }

    public State subtractValue(int addressOrValue) {
        accumulator -= addressOrValue;
        return this;
    }

    public State addAddress(int address) {
        return addValue(memory.get(address));
    }

    public Result<State, RuntimeError> loadIndirect(int address) {
        return loadDirect(memory.get(address));
    }

    public State subtractAddress(int address) {
        return subtractValue(memory.get(address));
    }
}
