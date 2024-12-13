package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "Source", "src", "magma", "Main.java");
            final var input = Files.readString(source);

            final var targetDirectory = Paths.get(".", "CompiledTarget", "src", "magma");
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }

            final var target = targetDirectory.resolve("Main.mgs");
            Files.writeString(target, compileFromJavaToMagma(input));

            final var source0 = Files.readString(target);
            final var otherTarget = Paths.get(".", "NativeTarget", "src", "magma");
            if (!Files.exists(otherTarget)) {
                Files.createDirectories(otherTarget);
            }

            final var namespace = Collections.singletonList("magma");
            Files.writeString(otherTarget.resolve("Main.java"), compileFromMagmaToJava(source0, namespace));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compileFromMagmaToJava(String input, List<String> namespace) {
        var node = new MapNode().withStringList("segments", namespace);
        final var packageStatement = createPackageRule().generate(node).orElse("");
        return packageStatement + compileRoot(input, Main::compileMagmaRootMember);
    }

    private static PrefixRule createPackageRule() {
        return new PrefixRule("package ", new SuffixRule(new StringListRule("segments", "."), ";\n"));
    }

    private static String compileMagmaRootMember(String input) {
        final var stripped = input.strip();
        if (stripped.startsWith("import ")) {
            return stripped + "\n";
        }
        if (stripped.startsWith("class def ")) {
            final var name = stripped.substring("class def ".length(), stripped.indexOf("(")).strip();
            final var contentStart = stripped.indexOf('{');
            final var contentEnd = stripped.lastIndexOf('}');
            return "record " + name + "(){" + stripped.substring(contentStart + 1, contentEnd) + "}";
        }
        return input;
    }

    private static String compileFromJavaToMagma(String input) {
        return compileRoot(input, Main::compileJavaRootMember);
    }

    private static String compileRoot(String input, Function<String, String> mapper) {
        final var segments = split(input);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(mapper.apply(segment));
        }

        return output.toString();
    }

    private static String compileJavaRootMember(String segment) {
        final var stripped = segment.strip();
        if (createPackageRule().parse(stripped).isPresent()) return "";

        if (stripped.startsWith("import ")) return stripped + "\n";

        final var classIndex = stripped.indexOf("class");
        if (classIndex == -1) return segment;

        final var contentStart = stripped.indexOf("{");
        if (contentStart == -1) return segment;

        final var contentEnd = stripped.lastIndexOf('}');
        if (contentEnd == -1) return segment;

        final var name = stripped.substring(classIndex + "class".length(), contentStart).strip();
        return "class def " + name + "() => {\n" + stripped.substring(contentStart + 1, contentEnd).strip() + "}";
    }

    private static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
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
}
