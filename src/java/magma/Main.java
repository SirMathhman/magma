package magma;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static final int IN = 0;

    public static void main(String[] args) {
        var input = new LinkedList<Integer>();
        run(input).ifPresent(value -> System.out.println(value.display()));
    }

    private static Option<RuntimeError> run(LinkedList<Integer> input) {
        var memory = new ArrayList<>(List.of(1));
        var counter = 0;
        while (counter < memory.size()) {
            var instruction = memory.get(counter);
            final var opCode = (instruction >> 24) & 0xFF;
            final var addressOrValue = instruction & 0x00FFFFFF;

            counter++;

            final var option = process(input, memory, opCode, addressOrValue);
            if(option.isPresent()) return option;
        }

        return new None<>();
    }

    private static Option<RuntimeError> process(Deque<Integer> input, List<Integer> memory, int opCode, int addressOrValue) {
        if (opCode == IN) {
            if (input.isEmpty()) {
                return new Some<>(new RuntimeError("Input is empty."));
            } else {
                memory.set(addressOrValue, input.poll());
                return new None<>();
            }
        }

        return new Some<>(new RuntimeError("Invalid op code: " + opCode));
    }
}
