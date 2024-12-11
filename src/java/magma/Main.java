package magma;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

        Interpreter.interpreter(input).consume(state -> {
            final var joined = state.display();
            System.out.println(joined);
        }, err -> {
            final var state = err.left();
            final var error = err.right();

            System.err.println(error.display());
            System.err.println(state.display());
        });
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
        return List.of(
                LoadValue.of(0x100),
                StoreIndirect.of(STACK_POINTER),

                LoadValue.of(0x200),

                StoreDirect.of(SPILL),
                LoadDirect.of(STACK_POINTER),
                AddValue.of(1),
                StoreDirect.of(STACK_POINTER),
                LoadDirect.of(SPILL),

                StoreIndirect.of(STACK_POINTER),

                LoadDirect.of(STACK_POINTER),
                SubtractValue.of(1),
                StoreDirect.of(STACK_POINTER)
        );
    }

    private static Stream<Integer> set(int address, int instruction) {
        return Stream.of(
                InStore.of(address),
                instruction
        );
    }
}
