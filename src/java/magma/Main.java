package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            for (Path source : sources) {
                runWithSource(source);
            }
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSource(Path source) throws IOException, CompileException {
        final var relativeSourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
        final var targetParent = TARGET_DIRECTORY.resolve(relativeSourceParent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = source.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);
        final var target = targetParent.resolve(nameWithoutExt + ".mgs");
        final var input = Files.readString(source);
        Files.writeString(target, compile(input));
    }

    private static String compile(String root) throws CompileException {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);

        final var builder = new StringBuilder();
        for (String segment : segments) {
            builder.append(compileRootSegment(segment));
        }
        return builder.toString();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        throw new CompileException("Invalid root segment", rootSegment);
    }
}
