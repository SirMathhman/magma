package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
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
        return splitAndCompile(root, Main::compileRootMember);
    }

    private static String splitAndCompile(String root, Function<String, String> compiler) {
        final var segments = split(root);
        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compiler.apply(segment.strip()));
        }
        return output.toString();
    }

    private static List<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if(c == '}' && depth == 1) {
                depth--;
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

        final var classIndex = rootSegment.indexOf("class");
        if(classIndex != -1 ) {
            final var withoutKeyword = rootSegment.substring(classIndex + "class".length());
            final var contentStartIndex = withoutKeyword.indexOf("{");
            if (contentStartIndex  != -1) {
                final var name = withoutKeyword.substring(0, contentStartIndex).strip();
                final var content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
                final var compiled = splitAndCompile(content, Main::compileClassSegment);
                return "struct " + name + " {" + compiled + "\n}";
            }
        }

        return invalidate("root segment", rootSegment);
    }

    private static String invalidate(String type, String rootSegment) {
        System.err.println("Unknown " + type + ": " + rootSegment);
        return rootSegment;
    }

    private static String compileClassSegment(String classSegment) {
        final var paramStart = classSegment.indexOf('(');
        if(paramStart != -1) {
            final var substring = classSegment.substring(0, paramStart);
            final var index = substring.lastIndexOf(' ');
            if(index != -1) {
                final var name = substring.substring(index + 1);
                return "\n\tvoid " + name + "(){\n\t}";
            }
        }
        return invalidate("class segment", classSegment);
    }
}
