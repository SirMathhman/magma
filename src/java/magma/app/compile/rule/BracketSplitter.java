package magma.app.compile.rule;

import magma.java.JavaStreams;

import java.util.List;

public record BracketSplitter() implements Splitter {
    private static SplitState splitAtChar(SplitState state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();

        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    @Override
    public List<String> split(String input) {
        return JavaStreams.fromString(input)
                .foldLeft(new SplitState(), BracketSplitter::splitAtChar)
                .advance()
                .segments();
    }
}