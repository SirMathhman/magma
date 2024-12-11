package magma;

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
        return define(0, loadValue(0x100))
                .addAll(define(1, loadValue(0x200)))
                .list();
    }

    private static JavaList<Integer> loadValue(int value) {
        return new JavaList<Integer>().add(LoadValue.of(value));
    }

    private static JavaList<Integer> define(int offset, JavaList<Integer> loader) {
        return new JavaList<Integer>()
                .addAll(loader)
                .add(StoreDirect.of(SPILL))
                .addAll(move(offset))
                .add(LoadDirect.of(SPILL))
                .add(StoreIndirect.of(STACK_POINTER))
                .addAll(move(-offset));
    }

    private static JavaList<Integer> move(int offset) {
        if (offset == 0) return new JavaList<>();

        var instruction = offset > 0
                ? AddValue.of(offset)
                : SubtractValue.of(-offset);

        return new JavaList<Integer>()
                .add(LoadDirect.of(STACK_POINTER))
                .add(instruction)
                .add(StoreDirect.of(STACK_POINTER));
    }

    private static Stream<Integer> set(int address, int instruction) {
        return Stream.of(
                InStore.of(address),
                instruction
        );
    }
}
