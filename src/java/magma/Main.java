package magma;

import magma.java.JavaPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");
    private static int counter = 0;

    public static void main(String[] args) {
        JavaPaths.collect()
                .match(Main::compileSources, Optional::of)
                .ifPresent(Throwable::printStackTrace);
    }

    private static Optional<IOException> compileSources(Set<Path> sources) {
        return sources.stream()
                .map(Main::compileSource)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<IOException> compileSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var namespace = computeNamespace(parent);
        final var name = computeName(relative);

        if (namespace.size() >= 2 && namespace.subList(0, 2).equals(List.of("magma", "java"))) return Optional.empty();

        final var targetParent = namespace.stream().reduce(TARGET_DIRECTORY, Path::resolve, (_, next) -> next);
        final var target = targetParent.resolve(name + ".c");
        return ensureDirectory(targetParent).or(() -> compileFromSourceToTarget(source, target));
    }

    private static Optional<IOException> ensureDirectory(Path targetParent) {
        if (Files.exists(targetParent)) return Optional.empty();
        return JavaPaths.createDirectoriesSafe(targetParent);
    }

    private static Optional<IOException> compileFromSourceToTarget(Path source, Path target) {
        return JavaPaths.readSafe(source)
                .mapValue(Main::compile)
                .match(output -> JavaPaths.writeSafe(target, output), Optional::of);
    }

    private static String computeName(Path relative) {
        final var name = relative.getFileName().toString();
        return name.substring(0, name.indexOf('.'));
    }

    private static List<String> computeNamespace(Path parent) {
        return IntStream.range(0, parent.getNameCount())
                .mapToObj(parent::getName)
                .map(Path::toString)
                .toList();
    }

    private static String compile(String root) {
        return splitAndCompile(Main::splitByStatements, Main::compileRootMember, Main::merge, root);
    }

    private static String splitAndCompile(
            Function<String, List<String>> splitter,
            Function<String, String> compiler,
            BiFunction<StringBuilder, String, StringBuilder> merger,
            String input
    ) {
        final var segments = splitter.apply(input);
        var output = Optional.<StringBuilder>empty();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            final var compiled = compiler.apply(stripped);
            if (output.isEmpty()) {
                output = Optional.of(new StringBuilder(compiled));
            } else {
                output = output.map(inner -> merger.apply(inner, compiled));
            }
        }

        return output.map(StringBuilder::toString).orElse("");
    }

    private static StringBuilder merge(StringBuilder inner, String stripped) {
        return inner.append(stripped);
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
                final var compiled = splitAndCompile(Main::splitByStatements, Main::compileClassSegment, Main::merge, content);
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
            final var index = substring.indexOf('=');
            if (index != -1) {
                final var definition = substring.substring(0, index);
                final var compiled = compileValue(substring.substring(index + 1));
                return "\n\t" + compileDefinition(definition).orElseGet(() -> invalidate("definition", definition)) + " = " + compiled + ";";
            }
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
                            final var outputContent = splitAndCompile(Main::splitByStatements, Main::compileStatement, Main::merge, inputContent);

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
        if (statement.startsWith("for")) return "\n\t\tfor (;;) {\n\t\t}";
        if (statement.startsWith("while")) return "\n\t\twhile (1) {\n\t\t}";
        if (statement.startsWith("else")) return "\n\t\telse {\n\t\t}";

        if (statement.startsWith("return ")) {
            final var substring = statement.substring("return ".length());
            if (substring.endsWith(";")) {
                final var substring1 = substring.substring(0, substring.length() - ";".length());
                final var compiled = compileValue(substring1);
                return "\n\t\treturn " + compiled + ";";
            }
        }

        final var value = compileIf(statement);
        if (value.isPresent()) return value.get();

        final var index1 = statement.indexOf("=");
        if (index1 != -1) {
            final var substring = statement.substring(0, index1);
            final var substring1 = statement.substring(index1 + 1);
            if (substring1.endsWith(";")) {
                final var compiled = compileDefinition(substring).orElseGet(() -> invalidate("definition", substring));
                final var compiled1 = compileValue(substring1.substring(0, substring1.length() - ";".length()).strip());
                return "\n\t\t" + compiled + " = " + compiled1 + ";";
            }
        }

        if (statement.endsWith(";")) {
            final var newCaller = compileInvocation(statement.substring(0, statement.length() - ";".length()));
            if (newCaller.isPresent()) return "\n\t\t" + newCaller.get() + ";";
        }

        if (statement.endsWith(";")) {
            final var optional = compileDefinition(statement.substring(0, statement.length() - 1));
            if (optional.isPresent()) return optional.get();
        }

        return invalidate("statement", statement);
    }

    private static Optional<String> compileIf(String statement) {
        if (!statement.startsWith("if")) return Optional.empty();
        final var withoutKeyword = statement.substring("if".length());

        final var paramEnd = findConditionParamEnd(withoutKeyword);
        if (paramEnd.isEmpty()) return Optional.empty();

        final var conditionWithEnd = withoutKeyword.substring(0, paramEnd.get()).strip();
        if (!conditionWithEnd.startsWith("(")) return Optional.empty();

        final var condition = conditionWithEnd.substring(1);
        final var value = compileValue(condition);


        return Optional.of("\n\t\tif(" + value + "){\n\t\t}");
    }

    private static Optional<Integer> findConditionParamEnd(String substring) {
        var index = Optional.<Integer>empty();
        var depth = 0;
        for (int i = 0; i < substring.length(); i++) {
            final var c = substring.charAt(i);
            if (c == ')' && depth == 1) {
                index = Optional.of(i);
                break;
            } else {
                if (c == '(') depth++;
                if (c == ')') depth--;
            }
        }
        return index;
    }

    private static Optional<String> compileInvocation(String statement) {
        if (!statement.endsWith(")")) return Optional.empty();
        final var substring = statement.substring(0, statement.length() - ")".length());
        return findArgStart(substring).map(index -> {
            final var caller = substring.substring(0, index);
            final var substring1 = substring.substring(index + 1);
            final var compiled = splitAndCompile(Main::splitByValues, value -> compileValue(value.strip()), (inner, stripped) -> inner.append(", ").append(stripped), substring1);

            final var newCaller = compileValue(caller.strip());
            return newCaller + "(" + compiled + ")";
        });
    }

    private static Optional<Integer> findArgStart(String substring) {
        var depth = 0;
        for (int i = substring.length() - 1; i >= 0; i--) {
            final var c = substring.charAt(i);
            if (c == '(' && depth == 0) {
                return Optional.of(i);
            } else {
                if (c == ')') depth++;
                if (c == '(') depth--;
            }
        }
        return Optional.empty();
    }

    private static String compileValue(String input) {
        if (isSymbol(input.strip())) return input.strip();
        if (isNumber(input.strip())) return input.strip();

        if (input.startsWith("!")) return "!" + compileValue(input.substring(1));

        final var optional3 = compileConstruction(input);
        if (optional3.isPresent()) return optional3.get();

        final var stripped = input.strip();
        if (stripped.startsWith("\"") && stripped.endsWith("\"")) return stripped;

        if (stripped.startsWith("'") && stripped.endsWith("'")) return stripped;

        final var nameSlice = compileLambda(input);
        if (nameSlice.isPresent()) return nameSlice.get();

        final var optional1 = compileInvocation(input);
        if (optional1.isPresent()) return optional1.get();

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

        final var compiled = compileOperator(input, "+");
        if (compiled.isPresent()) return compiled.get();

        final var optional = compileOperator(input, "==");
        if (optional.isPresent()) return optional.get();

        final var optional2 = compileOperator(input, "!=");
        if (optional2.isPresent()) return optional2.get();

        final var index3 = stripped.indexOf('?');
        if (index3 != -1) {
            final var condition = stripped.substring(0, index3);
            final var substring = stripped.substring(index3 + 1);
            final var maybe = substring.indexOf(':');
            if (maybe != -1) {
                final var substring1 = substring.substring(0, maybe);
                final var substring2 = substring.substring(maybe + 1);
                return compileValue(condition) + " ? " + compileValue(substring1) + " : " + compileValue(substring2);
            }
        }

        return invalidate("value", input);
    }

    private static Optional<String> compileLambda(String input) {
        final var arrowIndex = input.indexOf("->");
        if (arrowIndex == -1) return Optional.empty();
        final var beforeArrow = input.substring(0, arrowIndex).strip();
        final var afterArrow = input.substring(arrowIndex + "->".length()).strip();

        final var maybeNames = findLambdaNames(beforeArrow);
        if (maybeNames.isEmpty()) return Optional.empty();

        final String compiled;
        if (afterArrow.startsWith("{") && afterArrow.endsWith("}")) {
            final var substring1 = afterArrow.substring(1, afterArrow.length() - 1);
            compiled = splitAndCompile(Main::splitByStatements, Main::compileStatement, Main::merge, substring1);
        } else {
            compiled = "\n\t\t\treturn " + compileValue(afterArrow) + ";";
        }

        final var joinedNames = maybeNames.get().stream()
                .map(name -> "auto " + name)
                .collect(Collectors.joining(", "));

        return Optional.of("auto " + getLambda__() + "(" + joinedNames + "){" + compiled + "\n\t\t}");
    }

    private static String getLambda__() {
        final var lambda = "_lambda" + counter + "_";
        counter++;
        return lambda;
    }

    private static Optional<List<String>> findLambdaNames(String nameSlice) {
        if (nameSlice.isEmpty()) return Optional.of(Collections.emptyList());
        if (isSymbol(nameSlice)) return Optional.of(List.of(nameSlice));

        if (!nameSlice.startsWith("(") || !nameSlice.endsWith(")")) return Optional.empty();

        final var args = nameSlice.substring(1, nameSlice.length() - 1).split(",");
        return Optional.of(Arrays.stream(args)
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList());
    }

    private static Optional<String> compileConstruction(String input) {
        if (!input.startsWith("new ")) return Optional.empty();
        final var substring = input.substring("new ".length());

        if (!substring.endsWith(")")) return Optional.empty();
        final var substring2 = substring.substring(0, substring.length() - ")".length());

        return findArgStart(substring2).map(index -> {
            final var caller = substring2.substring(0, index);
            final var compiled1 = compileType(caller.strip());

            final var substring1 = substring2.substring(index + 1);
            final var compiled = splitAndCompile(Main::splitByValues, value -> compileValue(value.strip()), Main::merge, substring1);

            return compiled1 + "(" + compiled + ")";
        });
    }

    private static Optional<String> compileOperator(String input, String operator) {
        final var index2 = input.indexOf(operator);
        if (index2 == -1) return Optional.empty();

        final var compiled = compileValue(input.substring(0, index2));
        final var compiled1 = compileValue(input.substring(index2 + operator.length()));
        return Optional.of(compiled + " " + operator + " " + compiled1);
    }

    private static boolean isNumber(String value) {
        final var value1 = value.startsWith("-")
                ? value.substring(1)
                : value;

        for (int i = 0; i < value1.length(); i++) {
            final var c = value1.charAt(i);
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private static boolean isSymbol(String value) {
        for (int i = 0; i < value.length(); i++) {
            final var c = value.charAt(i);
            if (Character.isLetter(c) || c == '_' || (i != 0 && Character.isDigit(c))) continue;
            return false;
        }

        return true;
    }

    private static String compileParams(ArrayList<String> inputParamsList) {
        Optional<StringBuilder> maybeOutputParams = Optional.empty();
        for (String inputParam : inputParamsList) {
            final var stripped = inputParam.strip();
            if (stripped.isEmpty()) continue;

            final var outputParam = compileDefinition(stripped)
                    .orElseGet(() -> invalidate("definition", stripped));

            maybeOutputParams = maybeOutputParams
                    .map(stringBuilder -> stringBuilder.append(", ").append(outputParam))
                    .or(() -> Optional.of(new StringBuilder(outputParam)));
        }

        return maybeOutputParams.map(StringBuilder::toString).orElse("");
    }

    private static Optional<String> compileDefinition(String input) {
        final var stripped = input.strip();
        final var separator = stripped.lastIndexOf(' ');
        if (separator == -1) return Optional.empty();

        final var inputParamType = stripped.substring(0, separator);
        final var paramName = stripped.substring(separator + 1);

        var index = -1;
        var depth = 0;
        for (int i = 0; i < inputParamType.length(); i++) {
            var c = inputParamType.charAt(i);
            if (c == ' ' && depth == 0) {
                index = i;
            } else {
                if (c == '>') depth++;
                if (c == '<') depth--;
            }
        }

        final var inputParamType1 = index == -1
                ? inputParamType
                : inputParamType.substring(index + 1);

        final var outputParamType = compileType(inputParamType1);
        return Optional.of(outputParamType + " " + paramName);
    }

    private static String compileType(String input) {
        if (input.equals("var")) return "auto";

        if (input.endsWith("[]")) return "Slice<" + input.substring(0, input.length() - 2) + ">";
        final var genStart = input.indexOf("<");
        if (genStart != -1) {
            final var caller = input.substring(0, genStart);
            final var substring = input.substring(genStart + 1);
            if (substring.endsWith(">")) {
                final var substring1 = substring.substring(0, substring.length() - ">".length());
                final var s = splitAndCompile(Main::splitByValues, Main::compileType, Main::merge, substring1);
                return caller + "<" + s + ">";
            }
        }

        if (isSymbol(input)) return input;
        return invalidate("type", input);
    }

    private static ArrayList<String> splitByValues(String inputParams) {
        final var inputParamsList = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, inputParams.length())
                .mapToObj(inputParams::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            if (c == ',' && depth == 0) {
                advance(inputParamsList, buffer);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
                if (c == '-') {
                    if (!queue.isEmpty() && queue.peek() == '>') {
                        buffer.append(queue.pop());
                    }
                }
                if (c == '<' || c == '(') depth++;
                if (c == '>' || c == ')') depth--;
            }
        }
        advance(inputParamsList, buffer);
        return inputParamsList;
    }
}
