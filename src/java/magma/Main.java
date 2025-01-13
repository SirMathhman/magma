package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try (final var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relativized = SOURCE_DIRECTORY.relativize(source);
                final var parent = relativized.getParent();
                final var name = relativized.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));
                final var targetParent = TARGET_DIRECTORY.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var target = targetParent.resolve(nameWithoutExt + ".c");
                final var input = Files.readString(source);
                Files.writeString(target, compileRoot(input));
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compileRoot(String root) {
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

        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment));
        }

        return output.toString();
    }

    private static String compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package")) return "";

        System.err.println("Invalid root segment: " + rootSegment);
        return rootSegment;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
