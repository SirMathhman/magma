package magma;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static magma.Operation.*;

public class Main {
    public static void main(String[] args) {
        var input = new LinkedList<>(Stream.of(
                setAbsolute(2, Jump.of(0)),
                setAbsolute(3, NoOp.empty()),
                setAbsolute(4, 6),
                setAbsolute(5, 0),
                List.of(
                        LoadValue.of(0x100),
                        StoreIndirectly.of(4)
                ),
                setAbsolute(2, Jump.of(3))
        ).flatMap(Collection::stream).toList());

        var memory = new ArrayList<>(Collections.singletonList(InAndStore.of(1)));
        var programCounter = 0;
        var accumulator = 0;

        while (programCounter < memory.size()) {
            final var instruction = memory.get(programCounter);
            final var opCode = instruction >> 24;
            final var addressOrValue = instruction & 0x00FFFFFF;

            programCounter++;

            final var operation = values()[opCode];
            switch (operation) {
                case InAndStore -> {
                    final var next = input.isEmpty() ? 0 : input.pollFirst();
                    set(memory, addressOrValue, next);
                    break;
                }
                case Jump -> {
                    programCounter = addressOrValue;
                    break;
                }
                case NoOp -> {
                    break;
                }
                case LoadValue -> {
                    accumulator = addressOrValue;
                    break;
                }
                case StoreIndirectly -> {
                    final var address = memory.get(addressOrValue);
                    set(memory, address, accumulator);
                }
            }
        }

        System.out.println(display(memory));
    }

    private static List<Integer> setAbsolute(int address, int instruction) {
        return List.of(InAndStore.of(address), instruction);
    }

    private static String display(ArrayList<Integer> memory) {
        return IntStream.range(0, memory.size())
                .mapToObj(index -> displayWithIndex(memory, index))
                .collect(Collectors.joining("\n"));
    }

    private static String displayWithIndex(ArrayList<Integer> memory, int index) {
        final var value = memory.get(index);
        final var instruction = Instruction.decode(value);
        return Integer.toHexString(index) + ") " + instruction;
    }

    private static void set(List<Integer> memory, int address, int value) {
        while (!(address < memory.size())) {
            memory.add(0);
        }
        memory.set(address, value);
    }
}
