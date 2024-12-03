package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.java.JavaOrderedMap;

public record State(Frame frame) {
    public State() {
        this(new Frame());
    }

    public State define(String name, Node type) {
        return new State(frame.define(name, type));
    }

    public Option<Integer> computeOffset(String name) {
        return frame.countBefore(name);
    }

    record Frame(JavaOrderedMap<String, Node> frame) {
        public Frame() {
            this(new JavaOrderedMap<>());
        }

        public Frame define(String name, Node type) {
            return new Frame(frame.put(name, type));
        }

        public Option<Integer> countBefore(String name) {
            return frame.findIndexAndValue(name).map(Tuple::left);
        }
    }
}
