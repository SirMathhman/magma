package magma.app;

import magma.app.compile.DividingState;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Splitter {
    static List<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var state = new DividingState(segments, buffer);

        var queue = new LinkedList<Character>();
        for (int i = 0; i < root.length(); i++) {
            queue.add(root.charAt(i));
        }

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = splitAtChar(state, c, queue);
        }

        return state.advance().segments();
    }

    private static DividingState splitAtChar(DividingState state, char c, Deque<Character> queue) {
        final var appended = state.append(c);

        if (c == '\"') {
            var current = appended;
            while (!queue.isEmpty()) {
                final var next = queue.pop();
                current = current.append(next);

                if(next == '\"') {
                     break;
                }
            }

            return current;
        }

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }
}
