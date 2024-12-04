package magma.compile.rule;

import java.util.List;

public class ValueSplitter implements Splitter {
    static State splitAtChar(State state, char c) {
        if (c == ',' && state.isLevel()) return state.advance();

        final var appended = state.append(c);
        if (c == '<') return appended.enter();
        if (c == '>') return appended.exit();
        return appended;
    }

    @Override
    public List<String> split(String input) {
        var state = new State();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments();
    }
}