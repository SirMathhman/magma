package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        final var segments = splitByStatements(root);
        final var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compiler.apply(segment.strip()));
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

        return invalidate("root segment", rootSegment);
    }

    private static String invalidate(String type, String rootSegment) {
        System.err.println("Unknown " + type + ": " + rootSegment);
        return rootSegment;
    }

    private static String compileClassSegment(String classSegment) {
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
                        final var inputParamsList = splitByValues(inputParams);
                        final var outputParams = compileParams(inputParamsList);
                        return "\n\t" + type + " " + name + "(" + outputParams + "){\n\t}";
                    }
                }
            }
        }
        return invalidate("class segment", classSegment);
    }

    private static String compileParams(ArrayList<String> inputParamsList) {
        Optional<StringBuilder> maybeOutputParams = Optional.empty();
        for (String inputParam : inputParamsList) {
            final var stripped = inputParam.strip();
            if (stripped.isEmpty()) continue;

            maybeOutputParams = compileParameter(maybeOutputParams, stripped);
        }

        return maybeOutputParams.map(StringBuilder::toString).orElse("");
    }

    private static Optional<StringBuilder> compileParameter(Optional<StringBuilder> maybeOutputParams, String stripped) {
        final var separator = stripped.lastIndexOf(' ');
        if (separator == -1) {
            System.err.println("Invalid param: " + stripped);
            return maybeOutputParams;
        }

        final var inputParamType = stripped.substring(0, separator);
        final var paramName = stripped.substring(separator + 1);

        final var outputParamType = inputParamType.endsWith("[]")
                ? "Slice<" + inputParamType.substring(0, inputParamType.length() - 2) + ">"
                : inputParamType;

        final var outputParam = outputParamType + " " + paramName;

        return maybeOutputParams
                .map(stringBuilder -> stringBuilder.append(", ").append(outputParam))
                .or(() -> Optional.of(new StringBuilder(outputParam)));
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
