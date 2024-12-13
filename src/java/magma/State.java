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
    private int accumulator;

    public State(Deque<Integer> input, List<Integer> memory, int programCounter, int accumulator) {
        this.programCounter = programCounter;
        this.input = input;
        this.memory = memory;
        this.accumulator = accumulator;
    }

    public State(Deque<Integer> input, List<Integer> memory) {
        this(input, memory, 0, 0);
    }

    static State set(State state, int address, int value) {
        set(state.memory, address, value);
        return state;
    }

    private static void set(List<Integer> memory, int address, int value) {
        while (!(address < memory.size())) {
            memory.add(0);
        }
        memory.set(address, value);
    }

    private static String displayWithIndex(List<Integer> memory, int index) {
        final var value = memory.get(index);
        final var instruction = Instruction.decode(value);
        return Integer.toHexString(index) + ") " + instruction;
    }

    String display() {
        return IntStream.range(0, memory.size())
                .mapToObj(index -> displayWithIndex(memory, index))
                .collect(Collectors.joining("\n"));
    }

    State next() {
        return jumpValue(programCounter + 1);
    }

    State storeIndirect(int addressOrValue) {
        final var address = memory.get(addressOrValue);
        return set(this, address, accumulator);
    }

    State inDirect(int addressOrValue) {
        final var input = this.input;
        final var next = input.isEmpty() ? 0 : input.pollFirst();
        return set(this, addressOrValue, next);
    }

    public State jumpValue(int programCounter) {
        this.programCounter = programCounter;
        return this;
    }

    public State loadValue(int accumulator) {
        this.accumulator = accumulator;
        return this;
    }

    public Optional<Instruction> current() {
        if (programCounter >= memory.size()) {
            return Optional.empty();
        } else {
            return Optional.of(Instruction.decode(memory.get(accumulator)));
        }
    }
}
