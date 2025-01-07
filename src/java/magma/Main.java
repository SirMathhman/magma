package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "Main.java");
            final var input = Files.readString(source);
            final var output = splitAndCompile(input);
            Files.writeString(source.resolveSibling("Main.c"), output);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String splitAndCompile(String root) throws CompileException {
        final var segments = split(root);
        return compileSegments(segments);
    }

    private static String compileSegments(List<String> segments) throws CompileException {
        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment));
        }

        return output.toString();
    }

    private static List<String> split(String root) {
        var state = new State();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments();
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';') return appended.advance();
        return appended;
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        throw new CompileException("Invalid root segment", rootSegment);
    }
}
