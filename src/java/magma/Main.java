package magma;

import magma.java.JavaPaths;

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

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        JavaPaths.collect()
                .match(Main::compileSources, Optional::of)
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

        var namespace = new ArrayList<String>();
        for (int i = 0; i < parent.getNameCount(); i++) {
            namespace.add(parent.getName(i).toString());
        }

        if (namespace.size() >= 2) {
            if (namespace.subList(0, 2).equals(List.of("magma", "java"))) return Optional.empty();
        }

        var targetParent = TARGET_DIRECTORY;
        for (var namespaceSegment : namespace) {
            targetParent = targetParent.resolve(namespaceSegment);
        }

        if (!Files.exists(targetParent)) {
            final var directoriesError = JavaPaths.createDirectoriesSafe(targetParent);
            if (directoriesError.isPresent()) {
                return directoriesError;
            }
        }

        final var name = relative.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return JavaPaths.readSafe(source).match(input -> JavaPaths.writeSafe(target, compile(input)), Optional::of);
    }

    private static String compile(String root) {
        return splitAndCompile(Main::splitByStatements, Main::compileRootMember, root);
    }

    private static String splitAndCompile(Function<String, List<String>> splitter, Function<String, String> compiler, String input) {
        final var segments = splitter.apply(input);
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
                final var popped = queue.pop();
                buffer.append(popped);
                if (popped == '\\') {
                    buffer.append(queue.pop());
                }

                buffer.append(queue.pop());
                continue;
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var next = queue.pop();
                    buffer.append(next);

                    if (next == '"') break;
                    if (next == '\\') {
                        buffer.append(queue.pop());
                    }
                }
            }

            if (c == ';' && depth == 0) {
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(segments, buffer);
                buffer = new StringBuilder();
            } else {
                if (c == '{' || c == '(') depth++;
                if (c == '}' || c == ')') depth--;
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
                final var compiled = splitAndCompile(Main::splitByStatements, Main::compileClassSegment, content);
                return "struct " + name + " {" + compiled + "\n}";
            }
        }

        if (rootSegment.contains("record")) return "struct Temp {\n}";
        if (rootSegment.contains("interface ")) return "struct Temp {\n}";

        return invalidate("root segment", rootSegment);
    }

    private static String invalidate(String type, String rootSegment) {
        System.err.println("Unknown " + type + ": " + rootSegment);
        return rootSegment;
    }

    private static String compileClassSegment(String classSegment) {
        if (classSegment.endsWith(";")) {
            final var substring = classSegment.substring(0, classSegment.length() - 1);
            return "\n\t" + compileDefinition(substring) + ";";
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
                            final var outputContent = splitAndCompile(Main::splitByStatements, Main::compileStatement, inputContent);

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
        if (statement.startsWith("for")) return "\n\t\tfor(;;){\nt\t\t}";
        if (statement.startsWith("while")) return "\n\t\twhile(1){\nt\t\t}";

        if (statement.startsWith("return ")) return "\n\t\treturn temp;";
        if (statement.startsWith("if")) return "\n\t\tif(temp){\n\t\t}";

        final var index1 = statement.indexOf("=");
        if (index1 != -1) {
            final var substring = statement.substring(0, index1);
            final var substring1 = statement.substring(index1 + 1);
            if (substring1.endsWith(";")) {
                final var compiled = compileDefinition(substring);
                final var compiled1 = compileValue(substring1.substring(0, substring1.length() - ";".length()).strip());
                return "\n\t\tint " + compiled + " = " + compiled1 + ";";
            }
        }

        if (statement.endsWith(");")) {
            final var substring = statement.substring(0, statement.length() - ");".length());
            var index = -1;
            var depth = 0;
            for (int i = substring.length() - 1; i >= 0; i--) {
                final var c = substring.charAt(i);
                if (c == '(' && depth == 0) {
                    index = i;
                    break;
                } else {
                    if (c == ')') depth++;
                    if (c == '(') depth--;
                }
            }

            if (index != -1) {
                final var caller = substring.substring(0, index);
                final var substring1 = substring.substring(index + 1);
                final var compiled = splitAndCompile(Main::splitByValues, value -> compileValue(value.strip()), substring1);

                final var newCaller = compileValue(caller.strip());
                return "\n\t\t" + newCaller + "(" + compiled + ");";
            }
        }

        return invalidate("statement", statement);
    }

    private static String compileValue(String input) {
        if (isSymbol(input.strip())) return input.strip();
        if (isNumber(input.strip())) return input.strip();

        if (input.startsWith("new ")) {
            return "temp()";
        }

        final var index = input.lastIndexOf('.');
        if (index != -1) {
            final var substring = input.substring(0, index);
            final var substring1 = input.substring(index + 1);
            return compileValue(substring) + "." + substring1;
        }

        final var index1 = input.lastIndexOf("::");
        if (index1 != -1) {
            final var substring = input.substring(0, index1);
            final var substring1 = input.substring(index1 + "::".length());
            return compileValue(substring) + "." + substring1;
        }

        final var index2 = input.indexOf('+');
        if (index2 != -1) {
            final var compiled = compileValue(input.substring(0, index2));
            final var compiled1 = compileValue(input.substring(index2 + 1));
            return compiled + " + " + compiled1;
        }

        final var stripped = input.strip();
        if (stripped.startsWith("\"") && stripped.endsWith("\"")) return stripped;

        return invalidate("value", input);
    }

    private static boolean isNumber(String value) {
        for (int i = 0; i < value.length(); i++) {
            final var c = value.charAt(i);
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private static boolean isSymbol(String value) {
        for (int i = 0; i < value.length(); i++) {
            final var c = value.charAt(i);
            if (Character.isLetter(c) || c == '_') continue;
            return false;
        }

        return true;
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
                buffer = new StringBuilder();
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
