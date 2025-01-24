package magma.app.pass;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.app.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public record State(Stack<List<Node>> frames) {
    public State() {
        this(new Stack<>());
    }

    private static Optional<Node> findInFrame(String value, List<Node> frame) {
        return frame.stream()
                .filter(definition -> definition.findString("name").orElse("").equals(value))
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
        return this.frames.size();
    }

    public Option<Node> find(String value) {
        return this.frames.stream()
                .map(frame -> findInFrame(value, frame))
                .flatMap(Optional::stream)
                .findFirst()
                .<Option<Node>>map(Some::new)
                .orElseGet(None::new);
    }
}
