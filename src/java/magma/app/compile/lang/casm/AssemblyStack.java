package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.app.compile.Node;
import magma.java.JavaList;

import static magma.Assembler.STACK_POINTER;
import static magma.app.compile.lang.casm.Instructions.instruct;

public record AssemblyStack(long count, long address) {
    private static final String CACHE = "cache";

    public AssemblyStack() {
        this(0, 0);
    }

    public Tuple<AssemblyStack, Tuple<Long, JavaList<Node>>> pushMultipleData(JavaList<JavaList<Node>> loaders) {
        final var initial = new Tuple<>(this, new JavaList<Node>());
        return loaders.stream().foldLeft(initial, (tuple, loader) -> {
            final var stack = tuple.left();
            final var instructions = tuple.right();
            final var loaded = stack.pushSingleData(loader);
            return loaded.mapRight(Tuple::right).mapRight(instructions::addAll);
        }).mapRight(right -> new Tuple<>(address, right));
    }

    public Tuple<AssemblyStack, Tuple<Long, JavaList<Node>>> pushSingleData(JavaList<Node> loader) {
        final var tuple = moveToAddress(count);

        final var stack = tuple.left();
        final var instructions = tuple.right()
                .addAll(loader)
                .addLast(instruct("stoi", STACK_POINTER));

        return new Tuple<>(stack.expand(), new Tuple<>(count, instructions));
    }

    private AssemblyStack expand() {
        return new AssemblyStack(count + 1, address);
    }

    private Tuple<AssemblyStack, JavaList<Node>> moveToAddress(long address) {
        return moveByOffset(address - this.address);
    }

    public Tuple<AssemblyStack, JavaList<Node>> moveByOffset(long offset) {
        if (offset == 0) return new Tuple<>(this, new JavaList<>());

        var deltaInstruction = offset > 0
                ? instruct("addv", offset)
                : instruct("subv", -offset);

        var instructions = new JavaList<Node>()
                .addLast(instruct("stod", CACHE))
                .addLast(instruct("ldd", STACK_POINTER))
                .addLast(deltaInstruction)
                .addLast(instruct("stod", STACK_POINTER))
                .addLast(instruct("ldd", CACHE));

        return new Tuple<>(new AssemblyStack(count, address + offset), instructions);
    }
}
