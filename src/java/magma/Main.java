package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relativized = SOURCE_DIRECTORY.relativize(source);
                final var parent = relativized.getParent();
                final var nameWithExt = relativized.getFileName().toString();
                final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

                final var targetParent = TARGET_DIRECTORY.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var input = Files.readString(source);
                final var output = compile(input);

                final var target = targetParent.resolve(name + ".c");
                Files.writeString(target, output);

                final var header = targetParent.resolve(name + ".h");
                Files.writeString(header, output);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String root) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            final var c = root.charAt(i);
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

        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment.strip()));
        }

        return output.toString();
    }

    private static String compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return "#include \"temp.h\"\n";
        if (rootSegment.contains("class")) {
            return "struct Temp {\n};";
        }
        return invalidate(rootSegment, "root segment");
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String invalidate(String input, String type) {
        System.err.println("Invalid " + type + ": " + input);
        return input;
    }
}
