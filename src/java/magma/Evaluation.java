package magma;

import java.util.Optional;

public record Evaluation(Memory memory, int programCounter) {
    public Evaluation(Memory memory) {
        this(memory, 0);
    }

    public Optional<Evaluation> inputDirect(long addressOrValue) {
        return Optional.empty();
    }
}