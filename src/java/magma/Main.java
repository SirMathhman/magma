package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (var stream = Files.walk(sourceDirectory)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                runWithSource(source, sourceDirectory);
            }
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSource(Path source, Path sourceDirectory) throws IOException, CompileException {
        final var relativized = sourceDirectory.relativize(source);
        final var name = relativized.getFileName().toString();
        final var index = name.indexOf('.');
        if(index == -1) throw new RuntimeException("Invalid file name: " + relativized);

        final var nameWithoutExt = name.substring(0, index);

        final var targetParent = Paths.get(".", "src", "c").resolve(relativized.getParent());
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var target = targetParent.resolve(nameWithoutExt + ".c");
        Files.writeString(target, compile(Files.readString(source)));
    }

    private static String compile(String root) throws CompileException {
        final var segments = split(root);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment));
        }

        return output.toString();
    }

    private static ArrayList<String> split(String root) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if(c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if(!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        throw new CompileException("Unknown root segment", rootSegment);
    }
}
