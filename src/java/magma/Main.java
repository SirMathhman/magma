package magma;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static magma.OpCode.*;

public class Main {
    public static final Instruction DEFAULT_INSTRUCTION = new Instruction(Nothing, 0);

    public static void main(String[] args) {
        final var instructions = new ArrayList<>(List.of(
                JumpToValue.of(6),
                100,
                200,
                LoadFromAddress.of(4),
                AddFromAddress.of(5),
                Halt.empty()
        ));

        final var input = assemble(instructions);
        run(input).consume(
                value -> System.out.println(value.display()),
                error -> System.err.println(error.display())
        );
    }

    private static Deque<Integer> assemble(List<Integer> instructions) {
        var input = new LinkedList<>(List.of(
                InAddress.of(2),
                JumpToValue.of(0)
        ));

        for (int i = 0; i < instructions.size(); i++) {
            int instruction = instructions.get(i);
            input.add(InAddress.of(3 + i));
            input.add(instruction);
        }

        input.addAll(List.of(
                InAddress.of(2),
                JumpToValue.of(3)
        ));

        return input;
    }

    private static Result<State, RuntimeError> run(Deque<Integer> input) {
        var state = new State();
        while (true) {
            final var instructionOption = state.findCurrentInstruction();
            if (instructionOption.isEmpty()) break;
            final var instruction = instructionOption.orElse(DEFAULT_INSTRUCTION);

            final var processedResult = process(state.next(), input, instruction);
            if (processedResult.isErr()) return processedResult.preserveErr(state);

            final var processedState = processedResult.findValue().orElse(new None<>());
            if (processedState.isEmpty()) break;
            state = processedState.orElse(state);
        }

        return new Ok<>(state);
    }

    private static Result<Option<State>, RuntimeError> process(State state, Deque<Integer> input, Instruction instruction) {
        final var addressOrValue = instruction.addressOrValue();
        return switch (instruction.opCode()) {
            case InAddress -> handleInAddress(state, input, instruction);
            case JumpToValue -> new Ok<>(new Some<>(state.jump(addressOrValue)));
            case Nothing -> new Ok<>(new Some<>(state));
            case Halt -> new Ok<>(new None<>());
            case OutValue -> {
                System.out.print((char) addressOrValue);
                yield new Ok<>(new Some<>(state));
            }
            case LoadFromAddress -> handleLoadFromAddress(state, addressOrValue);
            case AddFromAddress -> handleAddFromAddress(state, addressOrValue);
        };
    }

    private static Result<Option<State>, RuntimeError> handleAddFromAddress(State state, int addressOrValue) {
        return state.addFromAddress(addressOrValue)
                .<Result<Option<State>, RuntimeError>>map(value -> new Ok<>(new Some<>(value)))
                .orElseGet(() -> new Err<>(new RuntimeError("Invalid address: " + addressOrValue)));
    }

    private static Result<Option<State>, RuntimeError> handleLoadFromAddress(State state, int addressOrValue) {
        return state.loadFromAddress(addressOrValue)
                .<Result<Option<State>, RuntimeError>>map(value -> new Ok<>(new Some<>(value)))
                .orElseGet(() -> new Err<>(new RuntimeError("Invalid address: " + addressOrValue)));
    }

    private static Result<Option<State>, RuntimeError> handleInAddress(State state, Deque<Integer> input, Instruction instruction) {
        if (input.isEmpty()) {
            return new Err<>(new RuntimeError("Input is empty."));
        } else {
            final var copy = state.set(instruction.addressOrValue(), input.poll());
            return new Ok<>(new Some<>(copy));
        }
    }

}
