package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            runWithSources(sources, sourceDirectory);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSources(List<Path> sources, Path sourceDirectory) throws IOException, CompileException {
        for (Path source : sources) {
            runWithSource(sourceDirectory, source);
        }
    }

    private static void runWithSource(Path sourceDirectory, Path source) throws IOException, CompileException {
        final var relativized = sourceDirectory.relativize(source);
        final var parent = relativized.getParent();
        final var targetDirectory = Paths.get(".", "src", "c");
        final var targetParent = targetDirectory.resolve(parent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = relativized.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);

        final var input = Files.readString(source);
        final var output = compileRoot(input);

        final var target = targetParent.resolve(nameWithoutExt + ".c");
        Files.writeString(target, output);
    }

    private static String compileRoot(String root) throws CompileException {
        final var segments = split(root);
        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment.strip()));
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

    private static String compileRootSegment(String segment) throws CompileException {
        if (segment.startsWith("package ")) return "";
        if (segment.startsWith("import ")) return "#include \"temp.h\";\n";
        if (segment.contains("class ")) return "struct Temp {};";
        throw new CompileException("Unknown root segment", segment);
    }
}
