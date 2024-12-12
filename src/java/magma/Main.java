package magma;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static magma.Instruction.displayEncoded;
import static magma.Instruction.of;
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
        System.out.println(displayEncoded(program));

        final var setInstructions = IntStream.range(0, program.size())
                .mapToObj(index -> set(5 + index, program.get(index)))
                .flatMap(Function.identity());

        final var set = Stream.of(
                set(2, of(Jump, 0)),
                set(STACK_POINTER, 5 + program.size()),
                set(SPILL, 0)
        ).flatMap(Function.identity());

        return Stream.concat(Stream.concat(set, setInstructions), set(2, of(Jump, 5)));
    }

    private static List<Integer> createProgram() {
        return assign(0, value(0))
                .add(encode(new Node().withInt("ordinal", Halt.ordinal())))
                .list();
    }

    private static JavaList<Integer> value(int value) {
        return new JavaList<Integer>().add(of(LoadValue, value));
    }

    private static JavaList<Integer> assign(int offset, JavaList<Integer> loader) {
        return new JavaList<Integer>()
                .addAll(loader)
                .add(of(StoreDirect, SPILL))
                .addAll(move(offset))
                .add(of(LoadDirect, SPILL))
                .add(of(StoreIndirect, STACK_POINTER))
                .addAll(move(-offset));
    }

    private static JavaList<Integer> move(int offset) {
        if (offset == 0) return new JavaList<>();

        int instruction;
        if (offset > 0) {
            instruction = of(AddValue, offset);
        } else {
            int addressOrValue = -offset;
            instruction = of(SubtractValue, addressOrValue);
        }

        return new JavaList<Integer>()
                .add(of(LoadDirect, STACK_POINTER))
                .add(instruction)
                .add(of(StoreDirect, STACK_POINTER));
    }

    private static Stream<Integer> set(int address, int instruction) {
        return Stream.of(
                of(InStore, address),
                instruction
        );
    }
}
