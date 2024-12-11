package magma;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public class State {
    private final Deque<Integer> input;
    private final List<Integer> memory;
    private int programCounter;

    public State(Deque<Integer> input, List<Integer> memory, int programCounter) {
        this.input = input;
        this.memory = memory;
        this.programCounter = programCounter;
    }

    State inAndStore(int addressOrValue) {
        final var first = input.removeFirst();
        while (!(addressOrValue < memory.size())) {
            memory.add(0);
        }

        memory.set(addressOrValue, first);
        return this;
    }

    State next() {
        setProgramCounter(getProgramCounter() + 1);
        return this;
    }

    public Deque<Integer> getInput() {
        return input;
    }

    public List<Integer> getMemory() {
        return memory;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public Optional<Integer> current() {
        if (this.getProgramCounter() >= this.memory.size()) return Optional.empty();
        return Optional.of(this.memory.get(this.getProgramCounter()));
    }
}
