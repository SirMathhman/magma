package magma.app.assemble;

import magma.api.option.Option;

import java.util.List;

public final class State {
    private final int programCounter;
    private final int accumulator;
    private final Memory memory;

    public State(Memory memory, int programCounter, int accumulator) {
        this.programCounter = programCounter;
        this.memory = memory;
        this.accumulator = accumulator;
    }

    public State() {
        this(new Memory(List.of(Operator.InAddress.of(1))), 0, 0);
    }

    public State storeDirectly(int addressOrValue) {
        return set(addressOrValue, getAccumulator());
    }

    public int getAccumulator() {
        return accumulator;
    }

    public Option<Instruction> findCurrentInstruction() {
        return memory.resolve(programCounter).map(Instruction::fromValue);
    }

    public State next() {
        return new State(memory, programCounter + 1, accumulator);
    }

    public State set(int address, int value) {
        return new State(memory.set(address, value), programCounter, accumulator);
    }

    public State jumpByValue(int address) {
        return new State(memory, address, accumulator);
    }

    public String display() {
        return toString();
    }

    @Override
    public String toString() {
        return "State[" +
                "\n\tprogramCounter=" + Integer.toHexString(programCounter) + "," +
                "\n\taccumulator=" + Integer.toHexString(accumulator) + "," +
                "\n\tmemory=" + memory.display() +
                "\n]";
    }

    public Option<State> loadFromAddress(int address) {
        return memory.resolve(address).map(value -> new State(memory, programCounter, value));
    }

    public Option<State> addFromAddress(int address) {
        return memory.resolve(address).map(value -> new State(memory, programCounter, accumulator + value));
    }

    public Option<State> jumpByAddress(int address) {
        return memory.resolve(address).map(this::jumpByValue);
    }

    public State invert() {
        return new State(memory, programCounter, accumulator > 0 ? 0 : 1);
    }

    public State subtract(int value) {
        return new State(memory, programCounter, accumulator - value);
    }

    public State add(int addressOrValue) {
        return new State(memory, programCounter, accumulator + addressOrValue);
    }

    public State jumpConditionByValue(int value) {
        if(accumulator < 0) {
            return jumpByValue(value);
        }
        return this;
    }

    public State loadFromValue(int value) {
        return new State(memory, programCounter, value);
    }

    public State storeIndirectly(int addressOrValue) {
        final var memory1 = memory.storeIndirectly(addressOrValue, accumulator);
        return new State(memory1, programCounter, accumulator);
    }
}
