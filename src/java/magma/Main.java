package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException, CompileException {
        final var source = Paths.get(".", "src", "java", "magma", "Main.java");
        final var input = Files.readString(source);
        final var output = compile(input);
        final var target = source.resolveSibling("Main.c");
        Files.writeString(target, output);
    }

    private static String compile(String root) throws CompileException {
        final var segments = split(root);
        var buffer = new StringBuilder();
        for (String segment : segments) {
            buffer.append(compileRootSegment(segment.strip()));
        }
        return buffer.toString();
    }

    private static ArrayList<String> split(String root) throws CompileException {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                if (!buffer.isEmpty()) segments.add(buffer.toString());
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }

        if (depth != 0) {
            throw new CompileException("Invalid depth");
        }

        if (!buffer.isEmpty()) segments.add(buffer.toString());
        return segments;
    }

    private static String compileRootSegment(String rootSegment) throws CompileException {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) {
            final var afterKeyword = rootSegment.substring("import ".length());
            if (afterKeyword.endsWith(";")) {
                final var namespace = Arrays.stream(afterKeyword.substring(0, afterKeyword.length() - ";".length())
                                .split("\\."))
                        .toList();

                final var generated = generate(new Node(namespace));
                if (generated.isPresent()) {
                    return generated.get();
                }
            }
        }

        if (rootSegment.contains("class ")) return "struct Temp {}";
        throw new CompileException("Invalid root", rootSegment);
    }

    private static Optional<String> generate(Node node) {
        final var namespace = node.findNamespace();
        if (namespace.isEmpty()) return Optional.empty();

        final var namespaceString = String.join("/", namespace.get());
        return Optional.of("#include <" + namespaceString + ".h>\n");
    }
}
