package magma;

import java.util.ArrayList;
import java.util.List;

public record Compiler(String input) {
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATEMENT_END = ";";
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String RECORD_KEYWORD_WITH_SPACE = "record ";
    public static final String RECORD_SUFFIX = "(){}";

    private static String compileRootMember(String input) throws CompileException {
        if (input.startsWith(PACKAGE_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return "";
        } else if (input.startsWith(IMPORT_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return input;
        } else if (input.startsWith(RECORD_KEYWORD_WITH_SPACE) && input.endsWith(RECORD_SUFFIX)) {
            final var name = input.substring(RECORD_KEYWORD_WITH_SPACE.length(), input.length() - RECORD_SUFFIX.length());
            return renderFunction(name);
        } else {
            throw new CompileException();
        }
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';') {
            return appended.advance();
        } else {
            return appended;
        }
    }

    static String renderFunction(String name) {
        return "class def " + name + "() => {}";
    }

    String compile() throws CompileException {
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