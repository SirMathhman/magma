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
            final var output = compile(input);
            final var target = source.resolveSibling("Main.c");
            Files.writeString(target, output);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String root) throws CompileException {
        final var segments = split(root);

        var output = new StringBuilder();
        for (String segment : segments) {
            output = output.append(compileRootMember(segment));
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
        if (c == ';') {
            return appended.advance();
        } else {
            return appended;
        }
    }

    private static String compileRootMember(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        throw new CompileException("Invalid root segment", rootSegment);
    }
}
