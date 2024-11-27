package magma;

import java.util.List;

public final class State {
    private final int programCounter;
    private final Memory memory;

    public State(Memory memory, int programCounter) {
        this.programCounter = programCounter;
        this.memory = memory;
    }

    public State() {
        this(new Memory(List.of(OpCode.InAddress.of(1))), 0);
    }

    Option<Instruction> findCurrentInstruction() {
        return memory.get(programCounter).map(Instruction::fromValue);
    }

    State next() {
        return new State(memory, programCounter + 1);
    }

    State set(int address, int value) {
        return new State(memory.set(address, value), programCounter);
    }

    public State jump(int address) {
        return new State(memory, address);
    }

    public String display() {
        return toString();
    }

    @Override
    public String toString() {
        return "State[" +
                "\n\tprogramCounter=" + programCounter + ", " +
                "\n\tmemory=" + memory.display() +
                "\n]";
    }
}
