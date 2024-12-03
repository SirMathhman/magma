package magma.app.compile.lang.casm;

import magma.app.compile.Node;
import magma.app.compile.lang.common.CommonLang;
import magma.app.compile.lang.common.Generator;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

import static magma.app.compile.lang.casm.CASMLang.LABEL_TYPE;
import static magma.app.compile.lang.casm.CASMLang.label;
import static magma.app.compile.lang.common.CommonLang.GROUP_CHILDREN;
import static magma.app.compile.lang.common.CommonLang.GROUP_TYPE;

public class FlattenLabel implements Stateless {
    private final Generator generator;

    public FlattenLabel() {
        generator = new Generator();
    }

    private State process(State state, Node child) {
        if (child.is(LABEL_TYPE)) {
            final var completed = state.completeCachedLabel();
            return completed.addLabel(child);
        } else if (child.is(GROUP_TYPE)) {
            return child.findNodeList(GROUP_CHILDREN)
                    .orElse(new JavaList<>())
                    .stream()
                    .foldLeft(state, this::process);
        } else {
            return state.addChild(child);
        }
    }

    @Override
    public Node afterPass(Node node) {
        final var name = node.findString("name").orElse("");
        final var children = node.findNodeList("children").orElse(new JavaList<>());

        var state = new State(generator, name);
        for (Node child : children.list()) {
            state = process(state, child);
        }

        final var completed = state.completeCachedLabel();
        return CommonLang.asGroup(completed.labels);
    }

    private record State(
            Generator generator,
            String currentName,
            JavaList<Node> labels,
            JavaList<Node> cachedChildren
    ) {
        public State(Generator generator, String name) {
            this(generator, name, new JavaList<>(), new JavaList<>());
        }

        private State addLabel(Node child) {
            return new State(generator, currentName, labels.add(child), cachedChildren);
        }

        private State completeCachedLabel() {
            final var captured = label(currentName, cachedChildren.list());
            final var list = labels.add(captured);
            return new State(generator, generator.createUniqueName("label"), list, new JavaList<>());
        }

        public State addChild(Node child) {
            return new State(generator, currentName, labels, cachedChildren.add(child));
        }
    }
}
