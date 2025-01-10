package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        collect().match(Main::compileSources, Optional::of)
                .ifPresent(Throwable::printStackTrace);
    }

    private static Optional<IOException> compileSources(Set<Path> sources) {
        for (Path source : sources) {
            final var maybeError = compileSource(source);
            if (maybeError.isPresent()) return maybeError;
        }

        return Optional.empty();
    }

    private static Optional<IOException> compileSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) {
            return JavaPaths.createDirectoriesSafe(targetParent);
        }

        final var name = relative.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return JavaPaths.readSafe(source).match(input -> JavaPaths.writeSafe(target, compile(input)), Optional::of);
    }

    private static Result<Set<Path>, IOException> collect() {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            return new Ok<>(stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static String compile(String root) {
        return splitAndCompile(root, Main::compileRootMember);
    }

    private static String splitAndCompile(String root, Function<String, String> compiler) {
        final var segments = splitByStatements(root);
        final var output = new StringBuilder();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;
            output.append(compiler.apply(stripped));
        }
        return output.toString();
    }

    private static List<String> splitByStatements(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, root.length())
                .mapToObj(root::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                buffer.append(queue.pop());
                buffer.append(queue.pop());
                continue;
            }

            if (c == ';' && depth == 0) {
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(segments, buffer);
        return segments;
    }

    private static void advance(List<String> segments, StringBuilder buffer) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static String compileRootMember(String rootSegment) {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return rootSegment + "\n";

        final var classIndex = rootSegment.indexOf("class");
        if (classIndex != -1) {
            final var withoutKeyword = rootSegment.substring(classIndex + "class".length());
            final var contentStartIndex = withoutKeyword.indexOf("{");
            if (contentStartIndex != -1) {
                final var name = withoutKeyword.substring(0, contentStartIndex).strip();
                final var content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
                final var compiled = splitAndCompile(content, Main::compileClassSegment);
                return "struct " + name + " {" + compiled + "\n}";
            }
        }

        if (rootSegment.contains("record")) {
            return "struct Temp {\n}";
        }

        return invalidate("root segment", rootSegment);
    }

    private static String invalidate(String type, String rootSegment) {
        System.err.println("Unknown " + type + ": " + rootSegment);
        return rootSegment;
    }

    private static String compileClassSegment(String classSegment) {
        if (classSegment.endsWith(";")) {
            final var substring = classSegment.substring(0, classSegment.length() - 1);
            return compileDefinition(substring);
        }

        final var paramStart = classSegment.indexOf('(');
        if (paramStart != -1) {
            final var beforeParamStart = classSegment.substring(0, paramStart);
            final var afterParamStart = classSegment.substring(paramStart + 1);

            final var paramEnd = afterParamStart.indexOf(')');
            if (paramEnd != -1) {
                final var nameSeparator = beforeParamStart.lastIndexOf(' ');
                if (nameSeparator != -1) {
                    final var beforeName = beforeParamStart.substring(0, nameSeparator);
                    final var typeSeparator = beforeName.lastIndexOf(' ');
                    if (typeSeparator != -1) {
                        final var type = beforeName.substring(typeSeparator + 1);
                        final var name = beforeParamStart.substring(nameSeparator + 1);
                        final var inputParams = afterParamStart.substring(0, paramEnd);
                        final var afterParams = afterParamStart.substring(paramEnd + 1).strip();
                        if (afterParams.startsWith("{") && afterParams.endsWith("}")) {
                            final var inputContent = afterParams.substring(1, afterParams.length() - 1);
                            final var outputContent = splitAndCompile(inputContent, Main::compileStatement);

                            final var inputParamsList = splitByValues(inputParams);
                            final var outputParams = compileParams(inputParamsList);

                            return "\n\t" + type + " " + name + "(" + outputParams + "){" + outputContent + "\n\t}";
                        }
                    }
                }
            }
        }
        return invalidate("class segment", classSegment);
    }

    private static String compileStatement(String statement) {
        if (statement.contains("=")) return "int temp = 0;";
        if (statement.endsWith(");")) return "\n\t\ttemp();";

        return invalidate("statement", statement);
    }

    private static String compileParams(ArrayList<String> inputParamsList) {
        Optional<StringBuilder> maybeOutputParams = Optional.empty();
        for (String inputParam : inputParamsList) {
            final var stripped = inputParam.strip();
            if (stripped.isEmpty()) continue;

            final var outputParam = compileDefinition(stripped);
            maybeOutputParams = maybeOutputParams
                    .map(stringBuilder -> stringBuilder.append(", ").append(outputParam))
                    .or(() -> Optional.of(new StringBuilder(outputParam)));
        }

        return maybeOutputParams.map(StringBuilder::toString).orElse("");
    }

    private static String compileDefinition(String input) {
        final var separator = input.lastIndexOf(' ');
        if (separator == -1) {
            System.err.println("Invalid param: " + input);
            return input;
        }

        final var inputParamType = input.substring(0, separator);
        final var paramName = input.substring(separator + 1);

        final var outputParamType = inputParamType.endsWith("[]")
                ? "Slice<" + inputParamType.substring(0, inputParamType.length() - 2) + ">"
                : inputParamType;

        return outputParamType + " " + paramName;
    }

    private static ArrayList<String> splitByValues(String inputParams) {
        final var inputParamsList = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < inputParams.length(); i++) {
            var c = inputParams.charAt(i);
            if (c == ',' && depth == 0) {
                advance(inputParamsList, buffer);
            } else {
                buffer.append(c);
                if (c == '<') depth++;
                if (c == '>') depth--;
            }
        }
        advance(inputParamsList, buffer);
        return inputParamsList;
    }
}
