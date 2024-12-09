package magma;

import java.util.Optional;

public record RuntimeError(String message, Optional<String> context) {
    public RuntimeError(String message) {
        this(message, Optional.empty());
    }

    public RuntimeError(String message, String context) {
        this(message, Optional.of(context));
    }

    public String display() {
        return message + context.map(inner -> ": " + inner).orElse(".");
    }
}
