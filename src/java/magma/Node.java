package magma;

import java.util.Optional;

public record Node(Optional<String> type, String value) {
    public Node retype(String type) {
        return new Node(Optional.of(type), value);
    }

    public boolean is(String type) {
        return this.type.isPresent() && this.type.get().equals(type);
    }
}
