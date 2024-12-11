package magma;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        var input = new LinkedList<Integer>();
        input.addLast(Operation.Halt.empty());

        var memory = new ArrayList<Integer>();
        memory.add(Operation.InAndStore.of(1));

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
        return state.current().map(Instruction::decode).flatMap(instruction -> {
            final var next = state.next();
            return switch (instruction.operation()) {
                case Nothing -> Optional.of(next);
                case InAndStore -> Optional.of(next.inAndStore(instruction.addressOrValue()));
                case Halt -> Optional.empty();
            };
        });
    }
}
