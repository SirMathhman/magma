package magma;

import java.util.Optional;

public final class Node {
    private final String value;
    private final Optional<String> type;

    public Node(Optional<String> type, String value) {
        this.value = value;
        this.type = type;
    }

    public Node(String value) {
        this(Optional.empty(), value);
    }

    public Node(String type, String value) {
        this(Optional.of(type), value);
    }

    public String value() {
        return this.value;
    }

    public boolean is(String type) {
        return this.type.isPresent() && this.type.get().equals(type);
    }
}
