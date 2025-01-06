package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

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
            output = output.append(compileRootMember(segment.strip()));
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
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static String compileRootMember(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return "#include \"temp.h\"\n";
        return compileClass(rootSegment).orElseThrow(() -> new CompileException("Invalid root segment", rootSegment));
    }

    private static Optional<String> compileClass(String rootSegment) {
        final var classIndex = rootSegment.indexOf("class ");
        if (classIndex == -1) return Optional.empty();
        final var afterKeyword = rootSegment.substring(classIndex + "class".length());
        final var contentStart = afterKeyword.indexOf('{');
        if (contentStart == -1) return Optional.empty();
        final var name = afterKeyword.substring(0, contentStart).strip();

        return Optional.of("struct " + name + " {};");
    }
}
