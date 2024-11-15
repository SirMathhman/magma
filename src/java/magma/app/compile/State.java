package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.None;
import magma.api.option.Option;
import magma.java.JavaList;
import magma.java.JavaOrderedMap;

public record State(JavaList<JavaOrderedMap<String, Node>> definitions) {
    public State() {
        this(new JavaList<JavaOrderedMap<String, Node>>().addLast(new JavaOrderedMap<>()));
    }

    public State enter() {
        return new State(definitions.addLast(new JavaOrderedMap<>()));
    }

    public State exit() {
        return new State(definitions.popLastAndDrop().orElse(definitions));
    }

    public Option<Tuple<State, JavaList<Node>>> loadLabel(String label) {
        return new None<>();
    }

    public Tuple<State, JavaList<Node>> define(String name, Node type) {
        return new Tuple<>(this, new JavaList<>());
    }
}
