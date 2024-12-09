package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

import static magma.Operation.InputDirect;
import static magma.Operation.apply;

public class Main {
    public static void main(String[] args) {
        run().consume(memory -> {
            final var joinedMemory = memory.display();
            System.out.println(joinedMemory);
        }, error -> System.err.println(error.display()));
    }

    private static Result<Memory, RuntimeError> run() {
        final var memory = new Memory()
                .set(1, InputDirect.instruct(1));

        var evaluation = new Evaluation(memory);
        while (true) {
            final var next = cycle(evaluation);
            if (next.isEmpty()) break;

            final var result = next.get();
            final var value = result.findValue();
            if (value.isPresent()) evaluation = value.get();

            final var error = result.findError();
            if (error.isPresent()) return new Err<>(error.get());
        }

        return new Ok<>(memory);
    }

    private static Optional<Result<Evaluation, RuntimeError>> cycle(Evaluation evaluation) {
        final var optional = evaluation.memory().get(evaluation.programCounter());
        if (optional.isEmpty()) return Optional.empty();

        final long instruction = optional.get();
        final var opCode = instruction >> 56;
        final var addressOrValue = instruction & 0x00FFFFFFFFFFFFFFL;

        final var operationOptional = apply((int) opCode);
        if (operationOptional.isEmpty())
            return Optional.of(new Err<>(new RuntimeError("Invalid opcode", String.valueOf(opCode))));

        if (operationOptional.get() == InputDirect) {
            return Optional.of(evaluation.inputDirect(addressOrValue)
                    .<Result<Evaluation, RuntimeError>>map(Ok::new)
                    .orElseGet(() -> new Err<>(new RuntimeError("No input present"))));
        }

        return Optional.empty();
    }
}
