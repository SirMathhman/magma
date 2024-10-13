package magma;

import java.util.ArrayList;
import java.util.List;

public record Compiler(String input) {
    private static String compileRootMember(String input) throws CompileException {
        final var parsed = JavaLang.PACKAGE_RULE.parse(input);

        if (input.startsWith("package ") && input.endsWith(CommonLang.STATEMENT_END)) {
            return "";
        }

        return CommonLang.IMPORT.parse(input)
                .flatMap(CommonLang.IMPORT::generate)
                .or(() -> JavaLang.RECORD.parse(input)
                        .flatMap(MagmaLang.FUNCTION_RULE::generate))
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