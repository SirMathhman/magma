package magma;

import java.util.ArrayList;
import java.util.List;

public record State(List<Integer> memory, int programCounter) {
    public State() {
        this(List.of(1), 0);
    }

    Option<Instruction> findCurrentInstruction() {
        if (programCounter >= memory.size()) {
            return new None<>();
        } else {
            var instruction = memory.get(programCounter);
            final var opCode = Main.OpCode.values()[(instruction >> 24) & 0xFF];
            final var addressOrValue = instruction & 0x00FFFFFF;
            return new Some<>(new Instruction(opCode, addressOrValue));
        }
    }

    State next() {
        return new State(memory, programCounter + 1);
    }

    State set(int address, Integer value) {
        final var copy = new ArrayList<>(memory());
        while (address >= copy.size()) {
            copy.add(0);
        }
        copy.set(address, value);
        return new State(copy, programCounter);
    }

    public State jump(int address) {
        return new State(memory, address);
    }
}
