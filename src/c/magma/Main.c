import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
struct Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relative = sourceDirectory.relativize(source);
                final var parent = relative.getParent();
                final var targetDirectory = Paths.get(".", "src", "c");
                final var targetParent = targetDirectory.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var name = relative.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));
                final var target = targetParent.resolve(nameWithoutExt + ".c");
                final var input = Files.readString(source);
                Files.writeString(target, compile(input));
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String root) {
        final var segments = split(root);
        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootMember(segment.strip()));
        }
        return output.toString();
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
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

    private static String compileRootMember(String rootSegment) {
        if(rootSegment.startsWith("package ")) return "";
        if(rootSegment.startsWith("import ")) return rootSegment + "\n";

        final var index = rootSegment.indexOf("class");
        final var index1 = rootSegment.indexOf("{");

        if(index != -1 && index1 != -1) {
            final var name = rootSegment.substring(index + "class".length(), index1).strip();
            final var content = rootSegment.substring(index1 + 1, rootSegment.length() - 1);
            return "struct " + name + " {" + content + "}";
        }

        System.err.println("Unknown root segment: " + rootSegment);
        return rootSegment;
    }
}