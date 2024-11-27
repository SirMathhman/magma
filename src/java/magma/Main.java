package magma;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static final int IN = 0;

    public static void main(String[] args) {
        var input = new LinkedList<Integer>(List.of(
                2
        ));

        run(input).ifPresent(value -> System.out.println(value.display()));
    }

    private static Option<RuntimeError> run(LinkedList<Integer> input) {
        List<Integer> memory = new ArrayList<>(List.of(1));
        var counter = 0;
        while (counter < memory.size()) {
            var instruction = memory.get(counter);
            final var opCode = (instruction >> 24) & 0xFF;
            final var addressOrValue = instruction & 0x00FFFFFF;

            counter++;

            final var result = process(input, memory, opCode, addressOrValue);
            if (result.isErr()) return result.findErr();
            else memory = result.findValue().orElse(memory);
        }

        return new None<>();
    }

    private static Result<List<Integer>, RuntimeError> process(Deque<Integer> input, List<Integer> memory, int opCode, int addressOrValue) {
        if (opCode == IN) {
            if (input.isEmpty()) {
                return new Err<>(new RuntimeError("Input is empty."));
            } else {
                final var copy = new ArrayList<>(memory);
                while (addressOrValue >= copy.size()) {
                    copy.add(0);
                }
                copy.set(addressOrValue, input.poll());
                return new Ok<>(copy);
            }
        }

        return new Err<>(new RuntimeError("Invalid op code: " + opCode));
    }
}
