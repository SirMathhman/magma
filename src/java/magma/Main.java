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
        return splitAndCompile(Main::splitByStatements, rootSegment -> compileRootMember(rootSegment, 1), Main::mergeStatements, root);
    }

    private static String splitAndCompile(
            Function<String, List<String>> splitter,
            Function<String, String> compiler,
            BiFunction<StringBuilder, String, StringBuilder> merger,
            String input
    ) {
        return splitter.apply(input)
                .stream()
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .reduce(Optional.<StringBuilder>empty(), (output, stripped) -> compileAndMerge(compiler, merger, output, stripped), (_, next) -> next)
                .map(StringBuilder::toString)
                .orElse("");
    }

    private static Optional<StringBuilder> compileAndMerge(
            Function<String, String> compiler,
            BiFunction<StringBuilder, String, StringBuilder> merger,
            Optional<StringBuilder> output,
            String stripped
    ) {
        final var compiled = compiler.apply(stripped);
        if (output.isEmpty()) {
            return Optional.of(new StringBuilder(compiled));
        } else {
            return output.map(inner -> merger.apply(inner, compiled));
        }
    }

    private static StringBuilder mergeStatements(StringBuilder inner, String stripped) {
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

    private static String compileRootMember(String rootSegment, int depth) {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return rootSegment + "\n";

        final var classIndex = rootSegment.indexOf("class");
        if (classIndex != -1) {
            final var withoutKeyword = rootSegment.substring(classIndex + "class".length());
            final var contentStartIndex = withoutKeyword.indexOf("{");
            if (contentStartIndex != -1) {
                final var name = withoutKeyword.substring(0, contentStartIndex).strip();
                final var content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
                final var compiled = splitAndCompile(Main::splitByStatements, classSegment -> compileClassSegment(classSegment, depth + 1), Main::mergeStatements, content);
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

    private static String compileClassSegment(String classSegment, int depth) {
        if (classSegment.endsWith(";")) {
            final var substring = classSegment.substring(0, classSegment.length() - 1);
            final var index = substring.indexOf('=');
            if (index != -1) {
                final var definition = substring.substring(0, index);
                final var compiled = compileValue(depth, substring.substring(index + 1));
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
                            final var outputContent = splitAndCompile(Main::splitByStatements, statement -> compileStatement(statement, depth), Main::mergeStatements, inputContent);
                            final var outputParams = splitAndCompile(Main::splitByValues, value -> compileDefinition(value).orElseGet(() -> invalidate("definition", value)), Main::mergeValues, inputParams);
                            return "\n\t" + type + " " + name + "(" + outputParams + "){" + outputContent + "\n\t}";
                        }
                    }
                }
            }
        }
        return invalidate("class segment", classSegment);
    }

    private static String compileStatement(String statement, int depth) {
        if (statement.strip().equals("continue;")) return generateStatement(depth, "continue");
        if (statement.strip().equals("break;")) return generateStatement(depth, "break");

        if (statement.startsWith("else")) {
            final var substring = statement.substring("else".length()).strip();
            final String output;
            if (substring.startsWith("{") && substring.endsWith("}")) {
                final var substring1 = substring.substring(1, substring.length() - 1);
                output = splitAndCompile(Main::splitByStatements, statement0 -> compileStatement(statement0, depth + 1), Main::mergeStatements, substring1);
            } else {
                output = compileStatement(substring, depth + 1);
            }

            final var indent = "\n" + "\t".repeat(depth);
            return indent + "else {" + output + indent + "}";
        }

        if (statement.startsWith("return ")) {
            final var substring = statement.substring("return ".length());
            if (substring.endsWith(";")) {
                final var substring1 = substring.substring(0, substring.length() - ";".length());
                final var compiled = compileValue(depth, substring1);
                return generateReturn(compiled, depth);
            }
        }

        final var optional1 = compileConditional(depth, "while", statement);
        if (optional1.isPresent()) return optional1.get();

        final var value = compileConditional(depth, "if", statement);
        if (value.isPresent()) return value.get();

        final var index1 = statement.indexOf("=");
        if (index1 != -1) {
            final var substring = statement.substring(0, index1);
            final var substring1 = statement.substring(index1 + 1);
            if (substring1.endsWith(";")) {
                final var compiled = compileDefinition(substring)
                        .or(() -> compileSymbol(substring))
                        .orElseGet(() -> invalidate("definition", substring));
                final var compiled1 = compileValue(depth, substring1.substring(0, substring1.length() - ";".length()).strip());
                return generateStatement(depth, compiled + " = " + compiled1);
            }
        }

        if (statement.endsWith(";")) {
            final var newCaller = compileInvocation(depth, statement.substring(0, statement.length() - ";".length()));
            if (newCaller.isPresent()) return generateStatement(depth, newCaller.get());
        }

        if (statement.endsWith(";")) {
            final var optional = compileDefinition(statement.substring(0, statement.length() - 1));
            if (optional.isPresent()) return optional.get();
        }

        return compilePostfix(statement, "--", depth)
                .or(() -> compilePostfix(statement, "++", depth))
                .orElseGet(() -> invalidate("statement", statement));

    }

    private static Optional<String> compilePostfix(String statement, String suffix, int depth) {
        final var joined = suffix + ";";
        if (!statement.endsWith(joined)) return Optional.empty();

        final var substring = statement.substring(0, statement.length() - (joined).length());
        return Optional.of(generateStatement(depth, compileValue(depth, substring) + suffix));
    }

    private static String generateStatement(int depth, String content) {
        return "\n" + "\t".repeat(depth) + content + ";";
    }

    private static String generateReturn(String compiled, int depth) {
        return "\n" + "\t".repeat(depth) + "return " + compiled + ";";
    }

    private static Optional<String> compileConditional(int depth, String prefix, String statement) {
        if (!statement.startsWith(prefix)) return Optional.empty();
        final var withoutKeyword = statement.substring(prefix.length());

        final var maybeParamEnd = findConditionParamEnd(withoutKeyword);
        if (maybeParamEnd.isEmpty()) return Optional.empty();

        final var paramEnd = maybeParamEnd.get();
        final var conditionWithEnd = withoutKeyword.substring(0, paramEnd).strip();
        final var content = withoutKeyword.substring(paramEnd + 1).strip();

        if (!conditionWithEnd.startsWith("(")) return Optional.empty();

        final var condition = conditionWithEnd.substring(1);
        final var value = compileValue(depth, condition);
        final String outputContent;
        if (content.startsWith("{") && content.endsWith("}")) {
            final var substring = content.substring(1, content.length() - 1);
            outputContent = splitAndCompile(Main::splitByStatements, statement1 -> compileStatement(statement1, depth + 1), Main::mergeStatements, substring);
        } else {
            outputContent = compileStatement(content, depth + 1);
        }

        final var indent = "\n" + "\t".repeat(depth);
        return Optional.of(indent + prefix + " (" + value + ") {" + outputContent + indent + "}");
    }

    private static Optional<Integer> findConditionParamEnd(String input) {
        final var queue = IntStream.range(0, input.length())
                .mapToObj(index -> new Tuple<>(index, input.charAt(index)))
                .collect(Collectors.toCollection(LinkedList::new));

        var depth = 0;
        while (!queue.isEmpty()) {
            final var popped = queue.pop();
            final var i = popped.left();
            final var c = popped.right();

            if (c == '\'') {
                final var popped1 = queue.pop().right();
                if (popped1 == '\\') {
                    queue.pop();
                }

                queue.pop();
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var next = queue.pop().right();
                    if (next == '"') break;
                    if (next == '\\') queue.pop();
                }
            }

            if (c == ')' && depth == 1) {
                return Optional.of(i);
            } else {
                if (c == '(') depth++;
                if (c == ')') depth--;
            }
        }

        return Optional.empty();
    }

    private static Optional<String> compileInvocation(int depth, String statement) {
        final var stripped = statement.strip();
        if (!stripped.endsWith(")")) return Optional.empty();
        final var substring = stripped.substring(0, stripped.length() - ")".length());
        return findArgStart(substring).map(index -> {
            final var caller = substring.substring(0, index);
            final var substring1 = substring.substring(index + 1);
            final var compiled = splitAndCompile(Main::splitByValues,
                    value -> compileValue(depth, value.strip()),
                    Main::mergeValues, substring1);

            final var newCaller = compileValue(depth, caller.strip());
            return newCaller + "(" + compiled + ")";
        });
    }

    private static Optional<Integer> findArgStart(String input) {
        return IntStream.range(0, input.length())
                .map(index -> input.length() - 1 - index)
                .mapToObj(index -> new Tuple<>(index, input.charAt(index)))
                .reduce(new Tuple<>(Optional.empty(), 0), Main::findArgStateFold, (_, next) -> next)
                .left();
    }

    private static Tuple<Optional<Integer>, Integer> findArgStateFold(Tuple<Optional<Integer>, Integer> previous, Tuple<Integer, Character> tuple) {
        final var previousOptional = previous.left();
        if (previousOptional.isPresent()) return previous;

        final var depth = previous.right();
        final var i = tuple.left();
        final var c = tuple.right();

        if (c == '(' && depth == 0) return new Tuple<>(Optional.of(i), depth);
        if (c == ')') return new Tuple<>(Optional.empty(), depth + 1);
        if (c == '(') return new Tuple<>(Optional.empty(), depth - 1);
        return new Tuple<>(Optional.empty(), depth);
    }

    private static String compileValue(int depth, String input) {
        return compileSymbol(input)
                .or(() -> compileNumber(input))
                .or(() -> compileString(input))
                .or(() -> compileChar(input))
                .or(() -> compileNot(depth, input))
                .or(() -> compileConstruction(depth, input))
                .or(() -> compileLambda(depth, input))
                .or(() -> compileInvocation(depth, input))
                .or(() -> compileAccess(depth, input, "."))
                .or(() -> compileAccess(depth, input, "::"))
                .or(() -> compileOperator(depth, input, "+"))
                .or(() -> compileOperator(depth, input, "-"))
                .or(() -> compileOperator(depth, input, "=="))
                .or(() -> compileOperator(depth, input, "!="))
                .or(() -> compileOperator(depth, input, "&&"))
                .or(() -> compileTernary(depth, input))
                .orElseGet(() -> invalidate("value", input));
    }

    private static Optional<String> compileNumber(String input) {
        final var stripped = input.strip();
        if (isNumber(stripped)) return Optional.of(stripped);
        return Optional.empty();
    }

    private static Optional<String> compileNot(int depth, String input) {
        if (input.startsWith("!")) return Optional.of("!" + compileValue(depth, input.substring(1)));
        return Optional.empty();
    }

    private static Optional<String> compileString(String input) {
        final var stripped = input.strip();
        if (stripped.startsWith("\"") && stripped.endsWith("\"")) return Optional.of(stripped);
        return Optional.empty();
    }

    private static Optional<String> compileChar(String input) {
        final var stripped = input.strip();
        if (stripped.startsWith("'") && stripped.endsWith("'")) return Optional.of(stripped);
        return Optional.empty();
    }

    private static Optional<String> compileAccess(int depth, String input, String slice) {
        final var index = input.lastIndexOf(slice);
        if (index == -1) return Optional.empty();

        final var substring = input.substring(0, index);
        final var substring1 = input.substring(index + slice.length());
        final var s = compileValue(depth, substring);
        return Optional.of(generateDataAccess(s, substring1));
    }

    private static String generateDataAccess(String s, String substring1) {
        return s + "." + substring1;
    }

    private static Optional<String> compileTernary(int depth, String stripped) {
        final var index3 = stripped.indexOf('?');
        if (index3 == -1) return Optional.empty();

        final var condition = stripped.substring(0, index3);
        final var substring = stripped.substring(index3 + 1);
        final var maybe = substring.indexOf(':');
        if (maybe == -1) return Optional.empty();

        final var ifTrue = substring.substring(0, maybe);
        final var ifFalse = substring.substring(maybe + 1);
        return Optional.of(compileValue(depth, condition) + " ? " + compileValue(depth, ifTrue) + " : " + compileValue(depth, ifFalse));
    }

    private static Optional<String> compileSymbol(String input) {
        final var stripped = input.strip();
        if (isSymbol(stripped)) return Optional.of(stripped);
        return Optional.empty();
    }

    private static Optional<String> compileLambda(int depth, String input) {
        final var arrowIndex = input.indexOf("->");
        if (arrowIndex == -1) return Optional.empty();
        final var beforeArrow = input.substring(0, arrowIndex).strip();
        final var afterArrow = input.substring(arrowIndex + "->".length()).strip();

        final var maybeNames = findLambdaNames(beforeArrow);
        if (maybeNames.isEmpty()) return Optional.empty();

        final String compiled;
        if (afterArrow.startsWith("{") && afterArrow.endsWith("}")) {
            final var substring1 = afterArrow.substring(1, afterArrow.length() - 1);
            compiled = splitAndCompile(Main::splitByStatements, statement -> compileStatement(statement, depth), Main::mergeStatements, substring1);
        } else {
            compiled = generateReturn(compileValue(depth, afterArrow), depth + 1);
        }

        final var joinedNames = maybeNames.get().stream()
                .map(name -> "auto " + name)
                .collect(Collectors.joining(", "));

        return Optional.of("auto " + createUniqueName() + "(" + joinedNames + "){" + compiled + "\n" + "\t".repeat(depth) + "}");
    }

    private static String createUniqueName() {
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

    private static Optional<String> compileConstruction(int depth, String input) {
        if (!input.startsWith("new ")) return Optional.empty();
        final var substring = input.substring("new ".length());

        if (!substring.endsWith(")")) return Optional.empty();
        final var withoutEnd = substring.substring(0, substring.length() - ")".length());

        return findArgStart(withoutEnd).map(index -> {
            final var caller = withoutEnd.substring(0, index);
            final var compiled1 = compileType(caller.strip());

            final var substring1 = withoutEnd.substring(index + 1);
            final var compiled = splitAndCompile(Main::splitByValues, value -> compileValue(depth, value.strip()), Main::mergeValues, substring1);

            return compiled1 + "(" + compiled + ")";
        });
    }

    private static StringBuilder mergeValues(StringBuilder inner, String stripped) {
        return inner.append(", ").append(stripped);
    }

    private static Optional<String> compileOperator(int depth, String input, String operator) {
        final var index2 = input.indexOf(operator);
        if (index2 == -1) return Optional.empty();

        final var compiled = compileValue(depth, input.substring(0, index2));
        final var compiled1 = compileValue(depth, input.substring(index2 + operator.length()));
        return Optional.of(compiled + " " + operator + " " + compiled1);
    }

    private static boolean isNumber(String value) {
        final var value1 = value.startsWith("-")
                ? value.substring(1)
                : value;

        return IntStream.range(0, value1.length())
                .mapToObj(value1::charAt)
                .allMatch(Character::isDigit);
    }

    private static boolean isSymbol(String value) {
        return IntStream.range(0, value.length())
                .mapToObj(index -> new Tuple<>(index, value.charAt(index)))
                .allMatch(Main::isSymbolChar);
    }

    private static boolean isSymbolChar(Tuple<Integer, Character> tuple) {
        final var i = tuple.left();
        final var c = tuple.right();
        return Character.isLetter(c) || c == '_' || (i != 0 && Character.isDigit(c));
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

        if (input.endsWith("[]")) return "Slice<" + input.substring(0, input.length() - "[]".length()) + ">";

        return compileGenericType(input)
                .or(() -> compileSymbol(input))
                .orElseGet(() -> invalidate("type", input));
    }

    private static Optional<String> compileGenericType(String input) {
        final var genStart = input.indexOf("<");
        if (genStart == -1) return Optional.empty();

        final var caller = input.substring(0, genStart);
        final var withEnd = input.substring(genStart + 1);
        if (!withEnd.endsWith(">")) return Optional.empty();

        final var inputArgs = withEnd.substring(0, withEnd.length() - ">".length());
        final var outputArgs = splitAndCompile(Main::splitByValues, Main::compileType, Main::mergeValues, inputArgs);
        return Optional.of(caller + "<" + outputArgs + ">");
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
