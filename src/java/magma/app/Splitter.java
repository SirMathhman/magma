package magma.app;

import magma.app.compile.CompileException;
import magma.app.compile.DividingState;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Splitter {
    static List<String> split(String root) throws CompileException {
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

        if (state.isLevel()) {
            return state.advance().segments();
        }
        throw new CompileException("State not level '" + state.depth() + "'", root);
    }

    private static DividingState splitAtChar(DividingState state, char c, Deque<Character> queue) {
        final var appended = state.append(c);

        if (c == '\'') {
            final var maybeEscape = queue.pop();
            final var withMaybeEscape = appended.append(maybeEscape);

            DividingState next;
            if (maybeEscape == '\\') {
                next = withMaybeEscape.append(queue.pop());
            } else {
                next = withMaybeEscape;
            }

            return next.append(queue.pop());
        }

        if (c == '\"') {
            var current = appended;
            while (!queue.isEmpty()) {
                final var next = queue.pop();
                current = current.append(next);

                if (next == '\"') {
                    break;
                }
            }

            return current;
        }

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
    }
}
