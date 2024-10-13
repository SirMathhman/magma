package magma;

import java.util.ArrayList;
import java.util.List;

public record Compiler(String input) {
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATEMENT_END = ";";
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String NAME = "name";
    public static final PrefixRule IMPORT = new PrefixRule(IMPORT_KEYWORD_WITH_SPACE, new SuffixRule(STATEMENT_END, new ExtractRule(NAME)));
    public static final String RECORD_KEYWORD_WITH_SPACE = "record ";
    public static final String RECORD_SUFFIX = "(){}";
    public static final PrefixRule RECORD = new PrefixRule(RECORD_KEYWORD_WITH_SPACE, new SuffixRule(RECORD_SUFFIX, new ExtractRule(NAME)));
    public static final String FUNCTION_PREFIX = "class def ";
    public static final String FUNCTION_SUFFIX = "() => {}";
    public static final SuffixRule FUNCTION = new SuffixRule(FUNCTION_SUFFIX, new PrefixRule(FUNCTION_PREFIX, new ExtractRule(NAME)));

    private static String compileRootMember(String input) throws CompileException {
        if (input.startsWith(PACKAGE_KEYWORD_WITH_SPACE) && input.endsWith(STATEMENT_END)) {
            return "";
        }

        return IMPORT.parse(input).flatMap(IMPORT::generate)
                .or(() -> RECORD.parse(input).flatMap(FUNCTION::generate))
                .orElseThrow(CompileException::new);
    }

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