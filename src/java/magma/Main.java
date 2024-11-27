package magma;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static magma.Main.OpCode.*;

public class Main {

    public static final Instruction DEFAULT_INSTRUCTION = new Instruction(Nothing, 0);

    public static void main(String[] args) {
        var input = new LinkedList<>(List.of(
                InAddress.of(2),
                JumpValue.of(0),
                InAddress.of(3),
                Halt.empty(),
                InAddress.of(2),
                JumpValue.of(3)
        ));

        run(input).consume(value -> System.out.println(value), error -> System.err.println(error.display()));
    }

    private static Result<State, RuntimeError> run(LinkedList<Integer> input) {
        var state = new State();
        while (true) {
            final var instructionOption = state.findCurrentInstruction();
            if (instructionOption.isEmpty()) break;
            final var instruction = instructionOption.orElse(DEFAULT_INSTRUCTION);

            final var preprocessed = state.next();
            final var processedResult = process(preprocessed, input, instruction);
            if (processedResult.isErr()) return processedResult.preserveErr(state);

            final var processedState = processedResult.findValue().orElse(new None<>());
            if (processedState.isEmpty()) break;
            state = processedState.orElse(preprocessed);
        }

        return new Ok<>(state);
    }

    private static Result<Option<State>, RuntimeError> process(State state, Deque<Integer> input, Instruction instruction) {
        return switch (instruction.opCode()) {
            case InAddress -> handleInAddress(state, input, instruction);
            case JumpValue -> new Ok<>(new Some<>(state.jump(instruction.addressOrValue())));
            case Halt -> new Ok<>(new None<>());
            default -> new Err<>(new RuntimeError("Invalid op code: " + instruction.opCode()));
        };
    }

    private static Result<Option<State>, RuntimeError> handleInAddress(State state, Deque<Integer> input, Instruction instruction) {
        if (input.isEmpty()) {
            return new Err<>(new RuntimeError("Input is empty."));
        } else {
            final var copy = state.set(instruction.addressOrValue(), input.poll());
            return new Ok<>(new Some<>(copy));
        }
    }

    enum OpCode {
        InAddress,
        JumpValue,
        Nothing,
        Halt;

        private int of(int addressOrValue) {
            final var opCode = IntStream.range(0, values().length)
                    .filter(index -> values()[index].equals(this))
                    .findFirst()
                    .orElse(0);

            return (opCode << 24) + addressOrValue;
        }

        public int empty() {
            return of(0);
        }
    }
}
