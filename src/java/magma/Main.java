package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var paths = stream.collect(Collectors.toSet());
            run(paths);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void run(Set<Path> paths) throws IOException, CompileException {
        final var sources = collectSources(paths);
        for (Path source : sources) {
            runWithSource(source);
        }
    }

    private static Set<Path> collectSources(Set<Path> paths) {
        final var sources = new HashSet<Path>();
        for (Path path : paths) {
            if (Files.isRegularFile(path) && path.toString().endsWith(".java")) {
                sources.add(path);
            }
        }
        return sources;
    }

    private static void runWithSource(Path source) throws IOException, CompileException {
        final var input = Files.readString(source);
        final var output = compile(input);

        final var relativized = Main.SOURCE_DIRECTORY.relativize(source.getParent());

        final var name = source.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var targetParent = TARGET_DIRECTORY.resolve(relativized);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var target = targetParent.resolve(nameWithoutExt + ".c");
        Files.writeString(target, output);
    }

    private static String compile(String root) throws CompileException {
        final var segments = split(root);

        var output = new StringBuilder();
        for (String rootSegment : segments) {
            output = output.append(compileRootSegment(rootSegment));
        }

        return output.toString();
    }

    private static List<String> split(String root) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var state = new State(segments, buffer);
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }
        return state.advance().getSegments();
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
        if (c == ';' && state.isLevel()) return appended.advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        throw new CompileException("Unknown root segment", rootSegment);
    }
}
