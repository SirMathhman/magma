package magma.app;

import magma.app.compile.DividingState;

import java.util.ArrayList;
import java.util.List;

public class Splitter {
    static List<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var state = new DividingState(segments, buffer);
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments();
    }

    private static DividingState splitAtChar(DividingState state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }
}
