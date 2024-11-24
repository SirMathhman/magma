package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        final var sources = collectSources();
        compileSources(sources);
    }

    private static void compileSources(Set<Path> sources) {
        try {
            for (var source : sources) {
                compileSource(source);
            }
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static void compileSource(Path source) throws IOException, CompileException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var nameWithExt = relativized.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(relativized.getParent());

        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);
        final var target = targetParent.resolve(name + ".mgs");

        final var input = Files.readString(source);
        Files.writeString(target, compile(input));
    }

    private static String compile(String input) throws CompileException {
        final var segments = split(input);

        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileRootSegment(segment.strip()));
        }

        return buffer.toString();
    }

    private static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        final var length = input.length();
        for (int i = 0; i < length; i++) {
            final var c = input.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootSegment(String input) throws CompileException {
        if (input.startsWith("package ")) return "";
        throw new CompileException("Invalid root", input);
    }

    private static Set<Path> collectSources() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
