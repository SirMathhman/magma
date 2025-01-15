package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relativized = sourceDirectory.relativize(source);
                final var parent = relativized.getParent();

                final var targetDirectory = Paths.get(".", "src", "c");
                final var targetParent = targetDirectory.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var name = relativized.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));

                final var input = Files.readString(source);
                final var output = compileRoot(input);

                final var header = targetParent.resolve(nameWithoutExt + ".h");
                Files.writeString(header, output);

                final var target = targetParent.resolve(nameWithoutExt + ".c");
                Files.writeString(target, output);
            }
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compileRoot(String root) throws CompileException {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            final var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        advance(buffer, segments);

        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment.strip()));
        }

        return output.toString();
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        throw new CompileException("Invalid root segment", rootSegment);
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
