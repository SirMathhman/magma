package magma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static magma.Operation.*;

public class Main {
    public static void main(String[] args) {
        var input = new LinkedList<Integer>(List.of(
                InAndStore.of(2),
                Jump.of(0),
                InAndStore.of(3),
                NoOp.empty(),
                InAndStore.of(2),
                Jump.of(3)
        ));

        var memory = new ArrayList<>(Collections.singletonList(InAndStore.of(1)));
        var programCounter = 0;

        while (programCounter < memory.size()) {
            final var instruction = memory.get(programCounter);
            final var opCode = instruction >> 24;
            final var addressOrValue = instruction & 0x00FFFFFF;

            programCounter++;

            final var operation = values()[opCode];
            switch (operation) {
                case InAndStore -> {
                    final var next = input.pollFirst();
                    set(memory, addressOrValue, next);
                }
                case Jump -> {
                    programCounter = addressOrValue;
                }
                case NoOp -> {
                }
            }
        }

        System.out.println(display(memory));
    }

    private static String display(ArrayList<Integer> memory) {
        return IntStream.range(0, memory.size())
                .mapToObj(index -> {
                    final var value = memory.get(index);
                    final var instruction = Instruction.decode(value);
                    return Integer.toHexString(index) + ") " + instruction;
                })
                .collect(Collectors.joining("\n"));
    }

    private static void set(List<Integer> memory, int address, int value) {
        while (!(address < memory.size())) {
            memory.add(0);
        }
        memory.set(address, value);
    }
}
