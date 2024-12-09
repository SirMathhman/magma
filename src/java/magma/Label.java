package magma;

import java.util.ArrayList;
import java.util.List;

public record Label(List<Instruction> instructions) {
    public Label() {
        this(new ArrayList<>());
    }

    Label instruct(List<Instruction> instructions) {
        final var copy = new ArrayList<>(this.instructions);
        copy.addAll(instructions);
        return new Label(copy);
    }
}