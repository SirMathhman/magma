package magma;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Function;

import static magma.Operation.InStore;

public class Interpreter {
    static Result<State, Tuple<State, RuntimeError>> interpreter(Deque<Integer> input) {
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

    static Optional<Result<State, RuntimeError>> cycle(State state) {
        return state.current()
                .map(Interpreter::decode)
                .flatMap(result -> processDecoded(state, result));
    }

    static Optional<Result<State, RuntimeError>> processDecoded(State state, Result<Instruction, RuntimeError> result) {
        return result.mapValue(instruction -> processInstruction(state, instruction))
                .into(Results::invertOption)
                .map(inner -> inner.flatMapValue(Function.identity()));
    }

    static Optional<Result<State, RuntimeError>> processInstruction(State state, Instruction instruction) {
        final var next = state.next();
        final var operation = instruction.operation();
        final var addressOrValue = instruction.addressOrValue();
        return switch (operation) {
            case Nothing -> Optional.of(new Ok<State, RuntimeError>(next));
            case InStore -> Optional.of(new Ok<State, RuntimeError>(next.inAndStore(addressOrValue)));
            case Halt -> Optional.empty();
            case Jump -> Optional.of(new Ok<State, RuntimeError>(next.jump(addressOrValue)));
            case LoadValue -> Optional.of(new Ok<State, RuntimeError>(next.loadValue(addressOrValue)));
            case LoadDirect -> Optional.of(next.loadDirect(addressOrValue));
            case StoreIndirect -> Optional.of(new Ok<State, RuntimeError>(next.storeIndirect(addressOrValue)));
            case StoreDirect -> Optional.of(new Ok<State, RuntimeError>(next.storeDirect(addressOrValue)));
            case AddValue -> Optional.of(new Ok<State, RuntimeError>(next.addValue(addressOrValue)));
            case AddDirect -> Optional.of(new Ok<State, RuntimeError>(next.addAddress(addressOrValue)));
            case SubtractValue -> Optional.of(new Ok<State, RuntimeError>(next.subtractValue(addressOrValue)));
            case LoadIndirect -> Optional.of(next.loadIndirect(addressOrValue));
        };
    }

    static Result<Instruction, RuntimeError> decode(Integer instruction1) {
        return Instruction.decode(instruction1)
                .<Result<Instruction, RuntimeError>>map(Ok::new)
                .orElseGet(() -> new Err<Instruction, RuntimeError>(new RuntimeError("Unknown instruction", Integer.toHexString(instruction1))));
    }
}