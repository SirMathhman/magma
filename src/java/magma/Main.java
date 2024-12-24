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
        final var generated = truncateLeft(rootSegment, "import ");
        if (generated.isPresent()) return generated.get();

        if (rootSegment.contains("class ")) return "struct Temp {}";
        throw new CompileException("Invalid root", rootSegment);
    }

    private static Optional<String> truncateLeft(String rootSegment, String prefix) {
        if (!rootSegment.startsWith(prefix)) return Optional.empty();
        final var afterKeyword = rootSegment.substring(prefix.length());
        return truncateRight(afterKeyword, ";");
    }

    private static Optional<String> truncateRight(String afterKeyword, String suffix) {
        if (afterKeyword.endsWith(suffix)) {
            final var substring = afterKeyword.substring(0, afterKeyword.length() - suffix.length());

            final var node = parseStringList(substring, "namespace");
            if (node.isPresent()) {
                final StringListRule namespace = new StringListRule("namespace", "/");
                final var generated = createIncludesRule(namespace).generate(node.get());
                if (generated.isPresent()) {
                    return generated;
                }
            }
        }
        return Optional.empty();
    }

    private static PrefixRule createIncludesRule(StringListRule namespace) {
        return new PrefixRule("#include <", new SuffixRule(namespace, ".h>\n"));
    }

    private static Optional<Node> parseStringList(String input, String propertyKey) {
        final var namespace = Arrays.stream(input.split("\\.")).toList();
        return Optional.of(new Node().withStringList(propertyKey, namespace));
    }
}
