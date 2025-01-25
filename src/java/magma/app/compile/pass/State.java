package magma.app.compile.pass;

import magma.api.option.Option;
import magma.app.compile.Input;
import magma.java.JavaOptions;
import magma.app.compile.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public record State(Stack<List<Node>> frames) {
    public State() {
        this(new Stack<>());
        this.frames.push(new ArrayList<>());
    }

    private static Optional<Node> findInFrame(String value, List<Node> frame) {
        return frame.stream()
                .filter(definition -> JavaOptions.toNative(definition.inputs().find("name").map(Input::unwrap)).orElse("").equals(value))
                .findFirst();
    }

    public State exit() {
        this.frames.pop();
        return this;
    }

    public State enter() {
        this.frames.push(new ArrayList<>());
        return this;
    }

    public State pushAll(List<Node> definitions) {
        this.frames.peek().addAll(definitions);
        return this;
    }

    public int depth() {
        return this.frames.size() - 1;
    }

    public Option<Node> find(String value) {
        return JavaOptions.fromNative(this.frames.stream()
                        .map(frame -> findInFrame(value, frame))
                        .flatMap(Optional::stream)
                        .findFirst());
    }
}
