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

    public static final int STACK_POINTER = 3;
    public static final int SPILL = 4;

    public static void main(String[] args) {
        var input = createLoadableProgram().collect(Collectors.toCollection(LinkedList::new));

        run(input).consume(state -> {
            final var joined = state.display();
            System.out.println(joined);
        }, err -> {
            final var state = err.left();
            final var error = err.right();

            System.err.println(error.display());
            System.err.println(state.display());
        });
    }

    private static Result<State, Tuple<State, RuntimeError>> run(LinkedList<Integer> input) {
        var memory = new ArrayList<Integer>();
        memory.add(InStore.of(1));

        var programCounter = 0;

        var state = new State(input, memory, programCounter);
        while (true) {
            final var optional = cycle(state);
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

    private static Stream<Integer> createLoadableProgram() {
        var program = createProgram();
        System.out.println(Instruction.displayEncoded(program));

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

    private static List<Integer> createProgram() {
        return Stream.of(
                define(1, loadValue(0x100)),
                define(2, Stream.of(
                        loadOffset(1)
                ).flatMap(Function.identity())),
                Stream.of(Halt.empty())
        ).flatMap(Function.identity()).collect(Collectors.toCollection(ArrayList::new));
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
                Stream.of(
                        Stream.of(StoreDirect.of(SPILL)),
                        moveToOffset(offset),
                        Stream.of(
                                LoadDirect.of(SPILL),
                                StoreIndirect.of(STACK_POINTER)
                        ),
                        moveToOffset(-offset)
                ).flatMap(Function.identity())
        ).flatMap(Function.identity());
    }

    private static Stream<Integer> moveToOffset(int offset) {
        if (offset == 0) return Stream.empty();
        var instruction = offset > 0
                ? AddValue.of(offset)
                : SubtractValue.of(-offset);

        return Stream.of(
                LoadDirect.of(STACK_POINTER),
                instruction,
                StoreDirect.of(STACK_POINTER)
        );
    }

    private static Stream<Integer> set(int address, int instruction) {
        return Stream.of(
                InStore.of(address),
                instruction
        );
    }

    private static Optional<Result<State, RuntimeError>> cycle(State state) {
        return state.current()
                .map(Main::decode)
                .flatMap(result -> processDecoded(state, result));
    }

    private static Optional<Result<State, RuntimeError>> processDecoded(State state, Result<Instruction, RuntimeError> result) {
        return result.mapValue(instruction -> processInstruction(state, instruction))
                .into(Results::invertOption)
                .map(inner -> inner.flatMapValue(Function.identity()));
    }

    private static Optional<Result<State, RuntimeError>> processInstruction(State state, Instruction instruction) {
        final var next = state.next();
        final var operation = instruction.operation();
        final var addressOrValue = instruction.addressOrValue();
        return switch (operation) {
            case Nothing -> Optional.of(new Ok<>(next));
            case InStore -> Optional.of(new Ok<>(next.inAndStore(addressOrValue)));
            case Halt -> Optional.empty();
            case Jump -> Optional.of(new Ok<>(next.jump(addressOrValue)));
            case LoadValue -> Optional.of(new Ok<>(next.loadValue(addressOrValue)));
            case LoadDirect -> Optional.of(next.loadDirect(addressOrValue));
            case StoreIndirect -> Optional.of(new Ok<>(next.storeIndirect(addressOrValue)));
            case StoreDirect -> Optional.of(new Ok<>(next.storeDirect(addressOrValue)));
            case AddValue -> Optional.of(new Ok<>(next.addValue(addressOrValue)));
            case AddAddress -> Optional.of(new Ok<>(next.addAddress(addressOrValue)));
            case SubtractValue -> Optional.of(new Ok<>(next.subtractValue(addressOrValue)));
            case LoadIndirect -> Optional.of(next.loadIndirect(addressOrValue));
        };
    }

    private static Result<Instruction, RuntimeError> decode(Integer instruction1) {
        return Instruction.decode(instruction1)
                .<Result<Instruction, RuntimeError>>map(Ok::new)
                .orElseGet(() -> new Err<>(new RuntimeError("Unknown instruction", Integer.toHexString(instruction1))));
    }
}
