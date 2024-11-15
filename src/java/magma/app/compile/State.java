package magma.app.compile;

import magma.api.option.Option;
import magma.app.compile.lang.casm.AssemblyStack;
import magma.java.JavaList;
import magma.java.JavaOrderedMap;

public record State(JavaList<JavaOrderedMap<String, Long>> frames, AssemblyStack stack) {
    public State() {
        this(new JavaList<JavaOrderedMap<String, Long>>().addLast(new JavaOrderedMap<>()), new AssemblyStack());
    }

    public State enter() {
        return new State(frames.addLast(new JavaOrderedMap<>()), stack);
    }

    public State exit() {
        return new State(frames.popLastAndDrop().orElse(frames), stack);
    }

    public State define(String name, long address) {
        return new State(frames.mapLast(last -> last.put(name, address)).orElse(frames), stack);
    }

    public int depth() {
        return Math.max(0, frames.size() - 1);
    }

    public Option<Long> lookup(String value) {
        return frames.findLast().flatMap(last -> last.find(value));
    }
}
