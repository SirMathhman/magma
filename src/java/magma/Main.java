package magma;

import java.util.*;
import java.util.stream.Stream;

import static magma.Operation.*;

public class Main {
    public static void main(String[] args) {
        var input = new LinkedList<>(Stream.of(
                setAbsolute(2, JumpValue.of(0)),
                setAbsolute(3, NoOp.empty()),
                setAbsolute(4, 6),
                setAbsolute(5, 0),
                List.of(
                        LoadValue.of(0x100),
                        StoreIndirect.of(4)
                ),
                setAbsolute(2, JumpValue.of(3))
        ).flatMap(Collection::stream).toList());

        var memory = new ArrayList<>(Collections.singletonList(InDirect.of(1)));

        var state = new State(input, memory);
        while (true) {
            final var current = state.current();
            if (current.isEmpty()) break;

            final var instruction = current.get();
            state = cycle(state.next(), instruction);
        }

        System.out.println(state.display());
    }

    private static State cycle(State state, Instruction instruction) {
        final var operation = instruction.operation();
        final var addressOrValue = instruction.addressOrValue();

        return switch (operation) {
            case NoOp -> state;
            case InDirect -> state.inDirect(addressOrValue);
            case JumpValue -> state.jumpValue(addressOrValue);
            case LoadValue -> state.loadValue(addressOrValue);
            case StoreIndirect -> state.storeIndirect(addressOrValue);
        };
    }

    private static List<Integer> setAbsolute(int address, int instruction) {
        return List.of(InDirect.of(address), instruction);
    }
}
