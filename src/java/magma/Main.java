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
    private static final Map<String, List<String>> unions = new HashMap<>();
    private static final Map<String, List<String>> structs = new HashMap<>();
    private static final Map<String, List<String>> functions = new HashMap<>();

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
            final var stripped = segment.strip();
            if (!stripped.isEmpty()) {
                output.append(compiler.apply(stripped));
            }
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
                .or(() -> compileTaggedBlock(input, "struct ", structs))
                .or(() -> compileTaggedBlock(input, "union ", unions))
                .or(() -> compileEnum(input))
                .or(() -> compileFunction(input))
                .orElse(input);
    }

    private static Optional<? extends String> compileFunction(String input) {
        final var paramStart = input.indexOf('(');
        if (paramStart == -1) return Optional.empty();

        final var inputDefinition = input.substring(0, paramStart).strip();
        final var afterParamStart = input.substring(paramStart + 1).strip();
        return define(inputDefinition, functions).flatMap(s -> {
            final var paramEnd = afterParamStart.indexOf(')');
            if (paramEnd == -1) return Optional.empty();

            final var params = Arrays.stream(afterParamStart.substring(0, paramEnd).split(","))
                    .map(String::strip)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.joining(", "));

            final var braced = afterParamStart.substring(paramEnd + 1).strip();
            if (!braced.startsWith("{") || !braced.endsWith("}")) return Optional.empty();

            final var inputContent = braced.substring(1, braced.length() - 1);
            final var outputContent = splitAndCompile(inputContent, Main::compileStatement);

            return Optional.of(s + "(" + params + "){" + outputContent + "\n}\n");
        });

    }

    private static Optional<String> define(String input, Map<String, List<String>> scope) {
        final var space = input.lastIndexOf(' ');
        if (space == -1) {
            return Optional.empty();
        }
        final var type = input.substring(0, space);
        final var name = input.substring(space + 1);

        final var joinedName = defineInScope(name, scope);
        final var resolved = resolveType(type);
        return Optional.of(resolved + " " + joinedName);
    }

    private static String compileStatement(String statement) {
        return compileDefinition(statement)
                .orElse("\n\t" + statement);
    }

    private static Optional<? extends String> compileEnum(String input) {
        if (!input.startsWith("enum")) return Optional.empty();
        final var content = input.substring("enum".length()).strip();

        final var start = content.indexOf("{");
        if (start == -1) return Optional.empty();
        final var substring = content.substring(start + 1);

        if (!substring.endsWith("}")) return Optional.empty();
        final var values = Arrays.stream(substring.substring(0, substring.length() - 1).split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(inner -> "\n\t" + inner)
                .collect(Collectors.joining(","));

        final var name = content.substring(0, start).strip();
        final var joinedName = defineInScope(name, enums);
        return Optional.of("enum " + joinedName + " {" + values + "\n};\n");
    }

    private static String defineInScope(String name, Map<String, List<String>> map) {
        final var qualifiedName1 = new ArrayList<>(namespace);
        qualifiedName1.add(name);

        map.put(name, qualifiedName1);
        return joinType(qualifiedName1);
    }

    private static Optional<String> compileTaggedBlock(String input, String prefix, Map<String, List<String>> map) {
        if (!input.startsWith(prefix)) return Optional.empty();

        final var contentStart = input.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var name = input.substring(prefix.length(), contentStart).strip();
        final var withEnd = input.substring(contentStart + 1);
        if (!withEnd.endsWith("}")) return Optional.empty();

        final var joinedName = defineInScope(name, map);
        final var content = withEnd.substring(0, withEnd.length() - 1);
        final var outputContent = splitAndCompile(content, Main::compileStructMember);
        return Optional.of(prefix + joinedName + " {" + outputContent + "\n};\n");
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
        final var newType = resolveType(oldType);

        return Optional.of("\n\t" + newType + " " + name + ";");
    }

    private static String resolveType(String type) {
        return compileEnumType(type, "enum ", enums)
                .or(() -> compileEnumType(type, "union ", unions))
                .or(() -> compileEnumType(type, "struct ", structs))
                .orElse(type);
    }

    private static Optional<String> compileEnumType(String type, String prefix, Map<String, List<String>> map) {
        if (map.containsKey(type)) return Optional.of(prefix + joinType(type, map));
        return Optional.empty();
    }

    private static String joinType(String oldType, Map<String, List<String>> map) {
        return joinType(map.get(oldType));
    }

    private static String joinType(List<String> elements) {
        return String.join("_", elements);
    }
}
