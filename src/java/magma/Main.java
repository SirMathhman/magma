package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Function;
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
        return splitAndCompile(root, Main::compileRootSegment);
    }

    private static String splitAndCompile(String input, Function<String, String> compiler) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
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

        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compiler.apply(segment.strip()));
        }

        return output.toString();
    }

    private static String compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return "#include \"temp.h\"\n";
        final var keyword = rootSegment.indexOf("class");
        if (keyword != -1) {
            final var afterKeyword = rootSegment.substring(keyword + "class".length());
            final var contentStart = afterKeyword.indexOf('{');
            if (contentStart != -1) {
                final var name = afterKeyword.substring(0, contentStart).strip();
                final var withEnd = afterKeyword.substring(contentStart + 1).strip();
                if (withEnd.endsWith("}")) {
                    final var content = withEnd.substring(0, withEnd.length() - 1);
                    final var outputContent = splitAndCompile(content, Main::compileStructSegment);
                    return "struct " + name + " {" + outputContent + "\n};";
                }
            }
        }
        return invalidate(rootSegment, "root segment");
    }

    private static String compileStructSegment(String structSegment) {
        final var index = structSegment.indexOf("=");
        if(index != -1) {
            final var stripped = structSegment.substring(0, index).strip();
            final var index1 = stripped.lastIndexOf(' ');
            if (index1 != -1) {
                final var name = stripped.substring(index1 + 1).strip();
                return "\n\tint " + name + " = 0;";
            }
        }
        return invalidate(structSegment, "struct segment");
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String invalidate(String input, String type) {
        System.err.println("Invalid " + type + ": " + input);
        return input;
    }
}
