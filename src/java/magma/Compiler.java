package magma;

import java.util.ArrayList;
import java.util.List;

public record Compiler(String input) {
    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';') {
            return appended.advance();
        } else {
            return appended;
        }
    }

    String compile() throws CompileException {
        final var segments = split();
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(JavaLang.JAVA_ROOT_MEMBER.parse(segment).flatMap(MagmaLang.MAGMA_ROOT_MEMBER::generate).orElse(""));
        }
        return buffer.toString();
    }

    private List<String> split() {
        final var length = input.length();
        var state = new State();

        for (int i = 0; i < length; i++) {
            final var c = input.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments;
    }

    private static class State {
        private final List<String> segments;
        private final StringBuilder buffer;

        private State() {
            this(new ArrayList<>(), new StringBuilder());
        }

        private State(List<String> segments, StringBuilder buffer) {
            this.buffer = buffer;
            this.segments = segments;
        }

        private State append(char c) {
            return new State(segments, buffer.append(c));
        }

        private State advance() {
            if (buffer.isEmpty()) return this;

            final var copy = new ArrayList<>(segments);
            copy.add(buffer.toString());
            return new State(copy, new StringBuilder());
        }
    }
}