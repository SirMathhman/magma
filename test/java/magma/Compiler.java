package magma;

import java.util.ArrayList;
import java.util.List;

public record Compiler(String input) {
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATEMENT_END = ";";

    private static String compileRootMember(String input) {
        if (input.startsWith(PACKAGE_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return "";
        }

        return input;
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';') {
            return appended.advance();
        } else {
            return appended;
        }
    }

    String compile() {
        final var segments = split();
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileRootMember(segment));
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