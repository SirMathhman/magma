package magma;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static magma.Operation.*;

public class Main {
    public static void main(String[] args) {
        var input = createProgram().collect(Collectors.toCollection(LinkedList::new));

        var memory = new ArrayList<Integer>();
        memory.add(InStore.of(1));

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

        final var joined = state.display();
        System.out.println(joined);
    }

    private static Stream<Integer> createProgram() {
        var program = new ArrayList<>(List.of(
                Halt.empty()
        ));

        final var setInstructions = IntStream.range(0, program.size())
                .mapToObj(index -> set(5 + index, program.get(index)))
                .flatMap(Function.identity());

        final var set = Stream.of(
                set(2, Jump.of(0)),
                set(3, 5 + program.size()),
                set(4, 0)
        ).flatMap(Function.identity());

        return Stream.concat(Stream.concat(set, setInstructions), set(2, Jump.of(5)));
    }

    private static Stream<Integer> set(int address, int instruction) {
        return Stream.of(
                InStore.of(address),
                instruction
        );
    }

    private static Optional<State> run(State state) {
        return state.current().map(Instruction::decode).flatMap(instruction -> {
            final var next = state.next();
            final var operation = instruction.operation();
            final var addressOrValue = instruction.addressOrValue();
            return switch (operation) {
                case Nothing -> Optional.of(next);
                case InStore -> Optional.of(next.inAndStore(addressOrValue));
                case Halt -> Optional.empty();
                case Jump -> Optional.of(next.jump(addressOrValue));
            };
        });
    }
}
