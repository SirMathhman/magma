package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try {
            run();
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void run() throws IOException, CompileException {
        final var sources = collect();
        for (Path source : sources) {
            runWithSource(source);
        }
    }

    private static Set<Path> collect() throws IOException {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        }
    }

    private static void runWithSource(Path source) throws IOException, CompileException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var relativeParent = relativized.getParent();
        final var relativeName = relativized.getFileName().toString();
        final var separator = relativeName.indexOf('.');
        final var name = relativeName.substring(0, separator);

        final var targetParent = TARGET_DIRECTORY.resolve(relativeParent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var target = targetParent.resolve(name + ".c");
        final var input = Files.readString(source);
        final var output = compileRoot(input);

        Files.writeString(target, output);
    }

    private static String compileRoot(String root) throws CompileException {
        final var segments = split(root);
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

        return state.advance().segments;
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        throw new CompileException("Invalid root segment", rootSegment);
    }
}
