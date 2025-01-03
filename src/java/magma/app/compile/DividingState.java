package magma.app.compile;

import java.util.ArrayList;
import java.util.List;

public record DividingState(List<String> segments, StringBuilder buffer, int depth) {
    public DividingState(List<String> segments, StringBuilder buffer) {
        this(segments, buffer, 0);
    }

    public DividingState append(char c) {
        return new DividingState(segments, buffer.append(c), depth);
    }

    public DividingState advance() {
        if (buffer.isEmpty()) {
            return this;
        }
        final var copy = new ArrayList<>(segments);
        copy.add(buffer().toString());
        return new DividingState(copy, new StringBuilder(), depth);
    }

    public boolean isLevel() {
        return depth == 0;
    }

    public DividingState enter() {
        return new DividingState(segments, buffer, depth + 1);
    }

    public DividingState exit() {
        return new DividingState(segments, buffer, depth - 1);
    }
}