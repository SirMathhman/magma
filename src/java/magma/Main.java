package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        var nodes = new ArrayList<Node>();
        for (String segment : segments) {
            final var parsed = createJavaRootSegmentRule()
                    .parse(segment.strip())
                    .orElseThrow(() -> new CompileException("Invalid root member", segment));
            nodes.add(parsed);
        }

        var buffer = new StringBuilder();
        for (Node node : nodes) {
            final var generated = createCRootSegmentRule()
                    .generate(node)
                    .orElseThrow(() -> new CompileException("Cannot generate", node.toString()));

            buffer.append(generated);
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

    private static OrRule createCRootSegmentRule() {
        return new OrRule(List.of(
                createIncludesRule(),
                new TypeRule("struct", new ExactRule("struct Temp {}"))
        ));
    }

    private static OrRule createJavaRootSegmentRule() {
        return new OrRule(List.of(
                createNamespacedRule("package", "package "),
                createNamespacedRule("import", "import ")
        ));
    }

    private static Rule createNamespacedRule(String type, String prefix) {
        final var namespace = new StringListRule("namespace", "\\.");
        return new TypeRule(type, new PrefixRule(prefix, new SuffixRule(namespace, ";")));
    }

    private static Rule createIncludesRule() {
        final var namespace = new StringListRule("namespace", "/");
        return new PrefixRule("#include <", new SuffixRule(namespace, ".h>\n"));
    }
}
