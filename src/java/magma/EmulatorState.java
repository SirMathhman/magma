package magma;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class EmulatorState {
    private final List<Long> memory;
    private final Port port;
    private long accumulator;
    private int programCounter;

    public EmulatorState(List<Long> memory, Port port) {
        this.memory = new ArrayList<>(memory);
        this.port = port;
        this.programCounter = 0;
        this.accumulator = 0;
    }

    @Override
    public String toString() {
        final var joinedMemory = memory.stream()
                .map(Long::toHexString)
                .collect(Collectors.joining(", ", "[", "]"));

        return "State{" +
                "memory=" + joinedMemory +
                ", programCounter=" + programCounter +
                '}';
    }

    public EmulatorState next() {
        programCounter++;
        return this;
    }

    public Optional<Long> current() {
        if (programCounter < memory.size()) {
            return Optional.of(memory.get(programCounter));
        } else {
            return Optional.empty();
        }
    }

    public EmulatorState inputDirect(long address) {
        final var read = port.read();

        if (read.isEmpty()) return this;
        final var next = read.orElse(0L);

        set((int) address, next);
        return this;
    }

    private void set(int address, Long value) {
        while (!(address < memory.size())) {
            memory.add(0L);
        }
        memory.set(address, value);
    }

    public EmulatorState jumpValue(long address) {
        if (address < memory.size()) {
            programCounter = (int) address;
        }
        return this;
    }

    public EmulatorState loadDirect(long address) {
        accumulator = memory.get((int) address);
        return this;
    }

    public EmulatorState addValue(long value) {
        accumulator += value;
        return this;
    }

    public EmulatorState storeDirect(long address) {
        set((int) address, accumulator);
        return this;
    }

    public EmulatorState loadValue(long value) {
        accumulator = value;
        return this;
    }

    public EmulatorState storeIndirect(long address) {
        if (address < memory.size()) {
            final var resolved = memory.get((int) address);
            set(Math.toIntExact(resolved), accumulator);
        }
        return this;
    }

    public EmulatorState subtractValue(long value) {
        accumulator -= value;
        return this;
    }

    public EmulatorState loadIndirect(long address) {
        final var next = memory.get((int) address);
        accumulator = memory.get(Math.toIntExact(next));
        return this;
    }

    public EmulatorState addDirect(long address) {
        accumulator += memory.get((int) address);
        return this;
    }

    public List<Long> getMemory() {
        return memory;
    }
}
