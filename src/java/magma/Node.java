package magma;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

public record Node(Option<String> type, String value) {
    public Node(String value) {
        this(new None<>(), value);
    }

    public Node retype(String type) {
        return new Node(new Some<>(type), value);
    }
}