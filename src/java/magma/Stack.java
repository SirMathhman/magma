package magma;

import java.util.ArrayList;
import java.util.List;

record Stack(List<List<Tuple<String, Long>>> frames) {
    public Stack() {
        this(new ArrayList<>(List.of(new ArrayList<>())));
    }

    public Stack exit() {
        frames.removeLast();
        return this;
    }

    public Stack enter() {
        frames.add(new ArrayList<>());
        return this;
    }

    public Stack define(String name, long size) {
        frames.getLast().add(new Tuple<>(name, size));
        return this;
    }

    public long resolveDataAddress(String name) {
        var sum = 0;

        for (List<Tuple<String, Long>> frame : frames()) {
            for (Tuple<String, Long> entry : frame) {
                final var left = entry.left();
                if (left.equals(name)) {
                    return sum;
                } else {
                    sum += entry.right();
                }
            }
        }

        return sum;
    }
}
