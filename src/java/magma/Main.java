package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    private static final Deque<String> namespace = new LinkedList<>();
    private static final Map<String, List<String>> enums = new HashMap<>();

    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "java", "magma", "main.mgs");
            final var input = Files.readString(source);
            final var target = source.resolveSibling("main.c");
            Files.writeString(target, compile(input));

            final var process = new ProcessBuilder("clang", "main.c", "-o", "main.exe")
                    .directory(Paths.get(".", "src", "java", "magma").toFile())
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String input) {
        return splitAndCompile(input, Main::compileRootSegment);
    }

    private static String splitAndCompile(String input, Function<String, String> compiler) {
        final var segments = split(input);

        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compiler.apply(segment.strip()));
        }

        return output.toString();
    }

    private static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootSegment(String input) {
        if (input.startsWith("import ") && input.endsWith(";")) {
            return "#include <" + input.substring("import ".length(), input.length() - 1) + ".h>\n";
        }

        return compileNamespace(input)
                .or(() -> compileStruct(input))
                .or(() -> compileEnum(input))
                .orElse(input);
    }

    private static Optional<? extends String> compileEnum(String input) {
        if (input.startsWith("enum")) {
            final var content = input.substring("enum".length()).strip();
            final var start = content.indexOf("{");

            if (start != -1) {
                final var name = content.substring(0, start).strip();
                final var qualifiedName = new ArrayList<>(namespace);
                qualifiedName.add(name);
                final var joinedName = String.join("_", qualifiedName);

                final var substring = content.substring(start + 1);
                if (substring.endsWith("}")) {
                    final var values = Arrays.stream(substring.substring(0, substring.length() - 1).split(","))
                            .map(String::strip)
                            .filter(value -> !value.isEmpty())
                            .map(inner -> "\n\t" + inner)
                            .collect(Collectors.joining(","));

                    enums.put(name, qualifiedName);
                    return Optional.of("enum " + joinedName + " {" + values + "\n};\n");
                }
            }
        }

        return Optional.empty();
    }

    private static Optional<String> compileStruct(String input) {
        if (!input.startsWith("struct ")) return Optional.empty();

        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var name = input.substring("struct ".length(), contentStart).strip();
        final var withEnd = input.substring(contentStart + 1);
        if (!withEnd.endsWith("}")) return Optional.empty();

        final var content = withEnd.substring(0, withEnd.length() - 1);
        final var outputContent = splitAndCompile(content, Main::compileStructMember);
        return Optional.of("struct " + name + " {" + outputContent + "\n};\n");
    }

    private static Optional<String> compileNamespace(String input) {
        if (!input.startsWith("namespace ")) return Optional.empty();

        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var name = input.substring("namespace ".length(), contentStart).strip();
        final var withEnd = input.substring(contentStart + 1);
        if (!withEnd.endsWith("}")) return Optional.empty();

        final var inputContent = withEnd.substring(0, withEnd.length() - 1);

        namespace.push(name);
        final var outputContent = splitAndCompile(inputContent, Main::compileRootSegment);
        namespace.pop();

        return Optional.of(outputContent);
    }

    private static String compileStructMember(String member) {
        return compileDefinition(member).orElse(member);
    }

    private static Optional<String> compileDefinition(String member) {
        if (!member.endsWith(";")) return Optional.empty();
        final var member1 = member.substring(0, member.length() - 1);

        final var separator = member1.lastIndexOf(' ');
        if (separator == -1) return Optional.empty();

        final var oldType = member1.substring(0, separator).strip();
        final var name = member1.substring(separator + 1).strip();

        final var newType = enums.containsKey(oldType)
                ? String.join("_", enums.get(oldType))
                : oldType;

        return Optional.of("\n\tenum " + newType + " " + name + ";");
    }
}
