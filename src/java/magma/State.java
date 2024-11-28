package magma;

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
        this(new Memory(List.of(OpCode.InAddress.of(1))), 0, 0);
    }

    Option<Instruction> findCurrentInstruction() {
        return memory.get(programCounter).map(Instruction::fromValue);
    }

    State next() {
        return new State(memory, programCounter + 1, accumulator);
    }

    State set(int address, int value) {
        return new State(memory.set(address, value), programCounter, accumulator);
    }

    public State jump(int address) {
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
        return memory.get(address).map(value -> new State(memory, programCounter, value));
    }

    public Option<State> addFromAddress(int address) {
        return memory.get(address).map(value -> new State(memory, programCounter, accumulator + value));
    }
}
