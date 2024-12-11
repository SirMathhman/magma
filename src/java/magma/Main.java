package magma;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        var input = new LinkedList<Integer>();
        input.addLast(instruct(Operation.Halt));

        var memory = new ArrayList<Integer>();
        memory.add(instruct(Operation.InAndStore, 1));

        var programCounter = 0;

        var state = new State(input, memory, programCounter);
        while (true) {
            final var optional = run(state);
            if (optional.isPresent()) {
                state = optional.get();
            } else {
                break;
            }
        }

        final var joined = state.getMemory().stream()
                .map(Integer::toHexString)
                .collect(Collectors.joining("\n"));

        System.out.println(joined);
    }

    private static Optional<State> run(State state) {
        return state.current().flatMap(instruction -> {
            final var opCode = instruction >> 24;
            final var addressOrValue = instruction & 0x00FFFFFF;

            final var next = state.next();
            final var operation = Operation.values()[opCode];
            return switch (operation) {
                case InAndStore -> Optional.of(next.inAndStore(addressOrValue));
                case Halt -> Optional.empty();
            };
        });
    }

    private static int instruct(Operation operation) {
        return instruct(operation, 0);
    }

    private static int instruct(Operation operation, int addressOrValue) {
        return (operation.ordinal() << 24) + addressOrValue;
    }
}
