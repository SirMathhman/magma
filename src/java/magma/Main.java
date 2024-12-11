package magma;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static magma.Operation.*;

public class Main {

    public static final int STACK_POINTER = 3;
    public static final int SPILL = 4;

    public static void main(String[] args) {
        var input = createProgram().collect(Collectors.toCollection(LinkedList::new));

        getState(input).consume(state -> {
            final var joined = state.display();
            System.out.println(joined);
        }, err -> {
            final var state = err.left();
            final var error = err.right();

            System.err.println(error.display());
            System.err.println(state.display());
        });
    }

    private static Result<State, Tuple<State, RuntimeError>> getState(LinkedList<Integer> input) {
        var memory = new ArrayList<Integer>();
        memory.add(InStore.of(1));

        var programCounter = 0;

        var state = new State(input, memory, programCounter);
        while (true) {
            final var optional = run(state);
            if (optional.isPresent()) {
                final var result = optional.get();
                final var value = result.findValue();
                if (value.isPresent()) {
                    state = value.orElseThrow();
                }

                final var error = result.findError();
                if (error.isPresent()) {
                    return new Err<>(new Tuple<>(state, error.orElseThrow()));
                }
            } else {
                break;
            }
        }

        return new Ok<>(state);
    }

    private static Stream<Integer> createProgram() {
        var program = Stream.of(
                define(0, loadValue(0x100)),
                define(1, loadOffset(0)),
                Stream.of(Halt.empty())
        ).flatMap(Function.identity()).collect(Collectors.toCollection(ArrayList::new));

        final var setInstructions = IntStream.range(0, program.size())
                .mapToObj(index -> set(5 + index, program.get(index)))
                .flatMap(Function.identity());

        final var set = Stream.of(
                set(2, Jump.of(0)),
                set(STACK_POINTER, 5 + program.size()),
                set(SPILL, 0)
        ).flatMap(Function.identity());

        return Stream.concat(Stream.concat(set, setInstructions), set(2, Jump.of(5)));
    }

    private static Stream<Integer> add(Stream<Integer> loadLeft, Stream<Integer> loadRight) {
        return Stream.of(
                loadLeft,
                Stream.of(StoreDirect.of(SPILL)),
                loadRight,
                Stream.of(AddAddress.of(SPILL))
        ).flatMap(Function.identity());
    }

    private static Stream<Integer> loadValue(int value) {
        return Stream.of(LoadValue.of(value));
    }

    private static Stream<Integer> loadOffset(int offset) {
        return Stream.of(
                moveToOffset(offset),
                Stream.of(LoadIndirect.of(STACK_POINTER)),
                moveToOffset(-offset)
        ).flatMap(Function.identity());
    }

    private static Stream<Integer> define(int offset, Stream<Integer> loader) {
        return Stream.of(
                loader,
                moveToOffset(offset),
                Stream.of(StoreIndirect.of(STACK_POINTER)),
                moveToOffset(offset)
        ).flatMap(Function.identity());
    }

    private static Stream<Integer> moveToOffset(int offset) {
        if (offset == 0) return Stream.empty();
        var instruction = offset > 0
                ? AddValue.of(offset)
                : SubtractValue.of(offset);

        return Stream.of(
                StoreDirect.of(SPILL),
                LoadDirect.of(STACK_POINTER),
                instruction,
                StoreDirect.of(STACK_POINTER),
                LoadDirect.of(SPILL)
        );
    }

    private static Stream<Integer> set(int address, int instruction) {
        return Stream.of(
                InStore.of(address),
                instruction
        );
    }

    private static Optional<Result<State, RuntimeError>> run(State state) {
        return state.current()
                .map(Main::decode)
                .flatMap(result -> result
                        .mapValue(instruction -> processInstruction(state, instruction))
                        .match(state0 -> state0.map(Ok::new), err -> Optional.of(new Err<>(err))));
    }

    private static Optional<State> processInstruction(State state, Instruction instruction) {
        final var next = state.next();
        final var operation = instruction.operation();
        final var addressOrValue = instruction.addressOrValue();
        return switch (operation) {
            case Nothing -> Optional.of(next);
            case InStore -> Optional.of(next.inAndStore(addressOrValue));
            case Halt -> Optional.empty();
            case Jump -> Optional.of(next.jump(addressOrValue));
            case LoadValue -> Optional.of(next.loadValue(addressOrValue));
            case StoreIndirect -> Optional.of(next.storeIndirect(addressOrValue));
            case StoreDirect -> Optional.of(next.storeDirect(addressOrValue));
            case LoadDirect -> Optional.of(next.loadDirect(addressOrValue));
            case AddValue -> Optional.of(next.addValue(addressOrValue));
            case SubtractValue -> Optional.of(next.subtractValue(addressOrValue));
            case AddAddress -> Optional.of(next.addAddress(addressOrValue));
            case LoadIndirect -> Optional.of(next.loadIndirect(addressOrValue));
        };
    }

    private static Result<Instruction, RuntimeError> decode(Integer instruction1) {
        return Instruction.decode(instruction1)
                .<Result<Instruction, RuntimeError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new RuntimeError("Unknown instruction", Integer.toHexString(instruction1))));
    }
}
