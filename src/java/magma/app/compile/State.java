package magma.app.compile;

import magma.app.compile.lang.casm.AssemblyStack;
import magma.java.JavaList;
import magma.java.JavaOrderedMap;

public record State(JavaList<JavaOrderedMap<String, Node>> frames, AssemblyStack stack) {
    public State() {
        this(new JavaList<JavaOrderedMap<String, Node>>().addLast(new JavaOrderedMap<>()), new AssemblyStack());
    }

    public State enter() {
        return new State(frames.addLast(new JavaOrderedMap<>()), stack);
    }

    public State exit() {
        return new State(frames.popLastAndDrop().orElse(frames), stack);
    }

    public State define(String name, Long address) {
        return this;
    }

    public int depth() {
        return Math.max(0, frames.size() - 1);
    }
}
