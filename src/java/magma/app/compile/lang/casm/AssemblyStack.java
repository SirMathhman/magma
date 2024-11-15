package magma.app.compile.lang.casm;

import magma.api.Tuple;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.java.JavaList;

import static magma.Assembler.STACK_POINTER;
import static magma.app.compile.lang.CASMLang.COMMENT_TYPE;
import static magma.app.compile.lang.CASMLang.COMMENT_VALUE;
import static magma.app.compile.lang.casm.Instructions.instruct;

public record AssemblyStack(long count, long address) {
    private static final String CACHE = "cache";

    public AssemblyStack() {
        this(0, 0);
    }

    private static Node comment(String value) {
        return new MapNode(COMMENT_TYPE)
                .withString(COMMENT_VALUE, value);
    }

    public Tuple<AssemblyStack, Tuple<Long, JavaList<Node>>> pushMultipleData(JavaList<JavaList<Node>> loaders) {
        final var initial = new Tuple<>(this, new JavaList<Node>());
        return loaders.streamWithIndex().foldLeft(initial, (tuple, loader) -> {
            final var stack = tuple.left();
            final var instructions = tuple.right();
            final var loaded = stack.pushSingleData(loader.right());
            return loaded.mapRight(Tuple::right)
                    .mapRight(right -> right.addFirst(comment("Push element " + loader.left())))
                    .mapRight(instructions::addAll);
        }).mapRight(right -> new Tuple<>(address, right));
    }

    public Tuple<AssemblyStack, Tuple<Long, JavaList<Node>>> pushSingleData(JavaList<Node> loader) {
        final var tuple = moveToEmptyAddress();

        final var stack = tuple.left();
        final var instructions = tuple.right()
                .addAll(loader)
                .addLast(instruct("stoi", STACK_POINTER));

        return new Tuple<>(stack.expand(), new Tuple<>(count, instructions));
    }

    public Tuple<AssemblyStack, JavaList<Node>> moveToEmptyAddress() {
        return moveToAddress(count)
                .mapRight(right -> right.isEmpty() ? right : right.addFirst(comment("move to empty address")));
    }

    private AssemblyStack expand() {
        return new AssemblyStack(count + 1, address);
    }

    public Tuple<AssemblyStack, JavaList<Node>> moveToAddress(long address) {
        return moveByOffset(address - this.address)
                .mapRight(right -> right.isEmpty() ? right : right.addFirst(comment("moving to " + address)));
    }

    public Tuple<AssemblyStack, JavaList<Node>> moveByOffset(long delta) {
        if (delta == 0) return new Tuple<>(this, new JavaList<>());

        var deltaInstruction = delta > 0
                ? instruct("addv", delta)
                : instruct("subv", -delta);

        var instructions = new JavaList<Node>()
                .addLast(instruct("stod", CACHE))
                .addLast(instruct("ldd", STACK_POINTER))
                .addLast(deltaInstruction)
                .addLast(instruct("stod", STACK_POINTER))
                .addLast(instruct("ldd", CACHE));

        return new Tuple<>(new AssemblyStack(count, address + delta), instructions);
    }
}
