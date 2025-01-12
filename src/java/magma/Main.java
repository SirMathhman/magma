package magma;

import magma.collect.List;
import magma.collect.Set;
import magma.io.Error;
import magma.io.Path;
import magma.collect.Deque;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.JavaOptionals;
import magma.java.JavaPaths;
import magma.java.JavaSet;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.stream.ArrayHead;
import magma.stream.Collectors;
import magma.stream.HeadedStream;
import magma.stream.LengthHead;
import magma.stream.Stream;
import magma.stream.Streams;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Main {
    public static final Path SOURCE_DIRECTORY = JavaPaths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = JavaPaths.get(".", "src", "c");

    private static int counter = 0;

    public static void main(String[] args) {
        SOURCE_DIRECTORY.walk()
                .mapValue(Main::filterPaths)
                .match(Main::compileSources, Some::new)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Option<Error> compileSources(Set<Path> sources) {
        return sources.stream()
                .map(Main::compileSource)
                .flatMap(Streams::fromOption)
                .next();
    }

    private static Option<Error> compileSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.findParent().orElse(JavaPaths.get("."));
        final var namespace = computeNamespace(parent);
        final var name = computeName(relative);

        final var namespaceSlice = namespace.slice(0, 2).orElse(new JavaList<>());
        if (namespaceSlice.equals(JavaList.of("magma", "java"))) return new None<>();

        final var targetParent = namespace.stream().foldLeft(TARGET_DIRECTORY, Path::resolve);
        final var target = targetParent.resolve(name + ".c");
        return ensureDirectory(targetParent).or(() -> compileFromSourceToTarget(source, target));
    }

    private static Option<Error> ensureDirectory(Path targetParent) {
        if (targetParent.exists()) return new None<>();
        return targetParent.createDirectories();
    }

    private static Option<Error> compileFromSourceToTarget(Path source, Path target) {
        return source.readString()
                .mapValue(Main::compile)
                .match(target::writeString, Some::new);
    }

    private static String computeName(Path relative) {
        final var name = relative.findFileName().toString();
        return name.substring(0, name.indexOf('.'));
    }

    private static List<String> computeNamespace(Path parent) {
        return parent.streamNames()
                .map(Path::toString)
                .collect(JavaList.collector());
    }

    private static String compile(String root) {
        return splitAndCompile(Main::splitByStatements, Main::compileRootMember, Main::mergeStatements, root);
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
                .<Option<StringBuilder>>foldLeft(new None<>(), (output, stripped) -> compileAndMerge(compiler, merger, output, stripped))
                .map(StringBuilder::toString)
                .orElse("");
    }

    private static Option<StringBuilder> compileAndMerge(
            Function<String, String> compiler,
            BiFunction<StringBuilder, String, StringBuilder> merger,
            Option<StringBuilder> output,
            String stripped
    ) {
        final var compiled = compiler.apply(stripped);
        if (output.isEmpty()) {
            return new Some<>(new StringBuilder(compiled));
        } else {
            return output.map(inner -> merger.apply(inner, compiled));
        }
    }

    private static StringBuilder mergeStatements(StringBuilder inner, String stripped) {
        return inner.append(stripped);
    }

    private static List<String> splitByStatements(String root) {
        var segments = new JavaList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = streamChars(root).collect(JavaLinkedList.collector());

        while (!queue.isEmpty()) {
            var c = popOrPanic(queue);
            buffer.append(c);

            if (c == '\'') {
                final var popped = popOrPanic(queue);
                buffer.append(popped);
                if (popped == '\\') {
                    buffer.append(popOrPanic(queue));
                }

                buffer.append(popOrPanic(queue));
                continue;
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var next = popOrPanic(queue);
                    buffer.append(next);

                    if (next == '"') break;
                    if (next == '\\') {
                        buffer.append(popOrPanic(queue));
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

    private static <T> T popOrPanic(Deque<T> queue) {
        return JavaOptionals.from(queue.pop()).map(Tuple::left).orElseThrow();
    }

    private static void advance(JavaList<String> segments, StringBuilder buffer) {
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
                final var compiled = splitAndCompile(Main::splitByStatements, Main::compileClassSegment, Main::mergeStatements, content);
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
                final var compiled = compileValue(2, substring.substring(index + 1));
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
                            final var outputContent = splitAndCompile(Main::splitByStatements, statement -> compileStatement(statement, 2), Main::mergeStatements, inputContent);
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

        final var Option1 = compileConditional(depth, "while", statement);
        if (Option1.isPresent()) return Option1.unwrap();

        final var value = compileConditional(depth, "if", statement);
        if (value.isPresent()) return value.unwrap();

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
            if (newCaller.isPresent()) return generateStatement(depth, newCaller.unwrap());
        }

        return compileDefinitionStatement(statement)
                .or(() -> compilePostfix(statement, "--", depth))
                .or(() -> compilePostfix(statement, "++", depth))
                .orElseGet(() -> invalidate("statement", statement));

    }

    private static Option<String> compileDefinitionStatement(String statement) {
        if (!statement.endsWith(";")) return new None<>();
        return compileDefinition(statement.substring(0, statement.length() - 1));
    }

    private static Option<String> compilePostfix(String statement, String suffix, int depth) {
        final var joined = suffix + ";";
        if (!statement.endsWith(joined)) return new None<>();

        final var substring = statement.substring(0, statement.length() - (joined).length());
        return new Some<>(generateStatement(depth, compileValue(depth, substring) + suffix));
    }

    private static String generateStatement(int depth, String content) {
        return "\n" + "\t".repeat(depth) + content + ";";
    }

    private static String generateReturn(String compiled, int depth) {
        return "\n" + "\t".repeat(depth) + "return " + compiled + ";";
    }

    private static Option<String> compileConditional(int depth, String prefix, String statement) {
        if (!statement.startsWith(prefix)) return new None<>();
        final var withoutKeyword = statement.substring(prefix.length());

        return findConditionParamEnd(withoutKeyword).flatMap(paramEnd -> {
            final var conditionWithEnd = withoutKeyword.substring(0, paramEnd).strip();
            final var content = withoutKeyword.substring(paramEnd + 1).strip();

            if (!conditionWithEnd.startsWith("(")) return new None<>();

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
            return new Some<>(indent + prefix + " (" + value + ") {" + outputContent + indent + "}");
        });
    }

    private static Option<Integer> findConditionParamEnd(String input) {
        final var queue = streamCharsWithIndices(input).collect(JavaLinkedList.collector());

        var depth = 0;
        while (!queue.isEmpty()) {
            final var popped = popOrPanic(queue);
            final var i = popped.left();
            final var c = popped.right();

            if (c == '\'') {
                final var popped1 = popOrPanic(queue).right();
                if (popped1 == '\\') {
                    popOrPanic(queue);
                }

                popOrPanic(queue);
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var next = popOrPanic(queue).right();
                    if (next == '"') break;
                    if (next == '\\') popOrPanic(queue);
                }
            }

            if (c == ')' && depth == 1) {
                return new Some<>(i);
            } else {
                if (c == '(') depth++;
                if (c == ')') depth--;
            }
        }

        return new None<>();
    }

    private static Option<String> compileInvocation(int depth, String statement) {
        final var stripped = statement.strip();
        if (!stripped.endsWith(")")) return new None<>();
        final var substring = stripped.substring(0, stripped.length() - ")".length());
        return findMatchingChar(substring, Main::streamReverseIndices, '(', ')', '(').map(index -> {
            final var caller = substring.substring(0, index);
            final var substring1 = substring.substring(index + 1);
            final var compiled = splitAndCompile(Main::splitByValues,
                    value -> compileValue(depth, value.strip()),
                    Main::mergeValues, substring1);

            final var newCaller = compileValue(depth, caller.strip());
            return newCaller + "(" + compiled + ")";
        });
    }

    private static Option<Integer> findMatchingChar(
            String input,
            Function<String, Stream<Integer>> streamer,
            char search,
            char enter,
            char exit
    ) {
        final var queue = streamer.apply(input)
                .extendBy(input::charAt)
                .collect(JavaLinkedList.collector());

        var current = new Tuple<Option<Integer>, Integer>(new None<>(), 0);
        while (!queue.isEmpty()) {
            final var tuple = popOrPanic(queue);
            current = findArgStateFold(current, tuple, search, enter, exit, queue);
        }

        return current.left();
    }

    private static Stream<Integer> streamReverseIndices(String input) {
        return new HeadedStream<>(new LengthHead(input.length())).map(index -> input.length() - 1 - index);
    }

    private static Tuple<Option<Integer>, Integer> findArgStateFold(
            Tuple<Option<Integer>, Integer> previous,
            Tuple<Integer, Character> tuple,
            char search,
            char enter,
            char exit,
            Deque<Tuple<Integer, Character>> queue) {
        final var previousOption = previous.left();
        if (previousOption.isPresent()) return previous;

        final var depth = previous.right();
        final var i = tuple.left();
        final var c = tuple.right();

        if (c == '\'') {
            final var popped = popOrPanic(queue);
            if (popped.right() == '\\') {
                popOrPanic(queue);
            }
            popOrPanic(queue);
        }

        if (c == search && depth == 0) return new Tuple<>(new Some<>(i), depth);
        if (c == enter) return new Tuple<>(new None<>(), depth + 1);
        if (c == exit) return new Tuple<>(new None<>(), depth - 1);
        return new Tuple<>(new None<>(), depth);
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

    private static Option<String> compileNumber(String input) {
        final var stripped = input.strip();
        if (isNumber(stripped)) return new Some<>(stripped);
        return new None<>();
    }

    private static Option<String> compileNot(int depth, String input) {
        if (input.startsWith("!")) return new Some<>("!" + compileValue(depth, input.substring(1)));
        return new None<>();
    }

    private static Option<String> compileString(String input) {
        final var stripped = input.strip();
        if (stripped.startsWith("\"") && stripped.endsWith("\"")) return new Some<>(stripped);
        return new None<>();
    }

    private static Option<String> compileChar(String input) {
        final var stripped = input.strip();
        if (stripped.startsWith("'") && stripped.endsWith("'")) return new Some<>(stripped);
        return new None<>();
    }

    private static Option<String> compileAccess(int depth, String input, String slice) {
        final var index = input.lastIndexOf(slice);
        if (index == -1) return new None<>();

        final var substring = input.substring(0, index);
        final var substring1 = input.substring(index + slice.length());
        final var s = compileValue(depth, substring);
        return new Some<>(generateDataAccess(s, substring1));
    }

    private static String generateDataAccess(String s, String substring1) {
        return s + "." + substring1;
    }

    private static Option<String> compileTernary(int depth, String stripped) {
        final var index3 = stripped.indexOf('?');
        if (index3 == -1) return new None<>();

        final var condition = stripped.substring(0, index3);
        final var substring = stripped.substring(index3 + 1);
        final var maybe = substring.indexOf(':');
        if (maybe == -1) return new None<>();

        final var ifTrue = substring.substring(0, maybe);
        final var ifFalse = substring.substring(maybe + 1);
        return new Some<>(compileValue(depth, condition) + " ? " + compileValue(depth, ifTrue) + " : " + compileValue(depth, ifFalse));
    }

    private static Option<String> compileSymbol(String input) {
        final var stripped = input.strip();
        if (isSymbol(stripped)) return new Some<>(stripped);
        return new None<>();
    }

    private static Option<String> compileLambda(int depth, String input) {
        final var arrowIndex = input.indexOf("->");
        if (arrowIndex == -1) return new None<>();
        final var beforeArrow = input.substring(0, arrowIndex).strip();
        final var afterArrow = input.substring(arrowIndex + "->".length()).strip();

        final var maybeNames = findLambdaNames(beforeArrow);
        if (maybeNames.isEmpty()) return new None<>();

        final String compiled;
        if (afterArrow.startsWith("{") && afterArrow.endsWith("}")) {
            final var substring1 = afterArrow.substring(1, afterArrow.length() - 1);
            compiled = splitAndCompile(Main::splitByStatements, statement -> compileStatement(statement, depth), Main::mergeStatements, substring1);
        } else {
            compiled = generateReturn(compileValue(depth, afterArrow), depth + 1);
        }

        return maybeNames.map(names -> {
            final var joinedNames = names.stream()
                    .map(name -> "auto " + name)
                    .collect(Collectors.joining(", "));

            return "auto " + createUniqueName() + "(" + joinedNames + "){" + compiled + "\n" + "\t".repeat(depth) + "}";
        });
    }

    private static String createUniqueName() {
        final var lambda = "_lambda" + counter + "_";
        counter++;
        return lambda;
    }

    private static Option<List<String>> findLambdaNames(String nameSlice) {
        if (nameSlice.isEmpty()) return new Some<>(new JavaList<>());
        if (isSymbol(nameSlice)) return new Some<>(JavaList.of(nameSlice));

        if (!nameSlice.startsWith("(") || !nameSlice.endsWith(")")) return new None<>();

        final var args = nameSlice.substring(1, nameSlice.length() - 1).split(",");
        return new Some<>(new HeadedStream<>(new ArrayHead<>(args))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .collect(JavaList.collector()));
    }

    private static Option<String> compileConstruction(int depth, String input) {
        if (!input.startsWith("new ")) return new None<>();
        final var substring = input.substring("new ".length());

        if (!substring.endsWith(")")) return new None<>();
        final var withoutEnd = substring.substring(0, substring.length() - ")".length());

        return findMatchingChar(withoutEnd, Main::streamReverseIndices, '(', ')', '(').map(index -> {
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

    private static Option<String> compileOperator(int depth, String input, String operator) {
        final var index2 = input.indexOf(operator);
        if (index2 == -1) return new None<>();

        final var compiled = compileValue(depth, input.substring(0, index2));
        final var compiled1 = compileValue(depth, input.substring(index2 + operator.length()));
        return new Some<>(compiled + " " + operator + " " + compiled1);
    }

    private static boolean isNumber(String value) {
        final var value1 = value.startsWith("-")
                ? value.substring(1)
                : value;

        return streamChars(value1)
                .collect(Collectors.allMatch(Character::isDigit));
    }

    private static Stream<Character> streamChars(String value1) {
        return new HeadedStream<>(new LengthHead(value1.length()))
                .map(value1::charAt);
    }

    private static boolean isSymbol(String value) {
        return streamCharsWithIndices(value).collect(Collectors.allMatch(Main::isSymbolChar));
    }

    private static Stream<Tuple<Integer, Character>> streamCharsWithIndices(String value) {
        return new HeadedStream<>(new LengthHead(value.length())).extendBy(value::charAt);
    }

    private static boolean isSymbolChar(Tuple<Integer, Character> tuple) {
        final var i = tuple.left();
        final var c = tuple.right();
        return Character.isLetter(c) || c == '_' || (i != 0 && Character.isDigit(c));
    }

    private static Option<String> compileDefinition(String input) {
        final var stripped = input.strip();
        final var separator = stripped.lastIndexOf(' ');
        if (separator == -1) return new None<>();

        final var inputParamType = stripped.substring(0, separator);
        final var paramName = stripped.substring(separator + 1);

        final var inputParamType1 = findMatchingChar(inputParamType, Main::streamReverseIndices, ' ', '>', '<')
                .map(index -> inputParamType.substring(index + 1))
                .orElse(inputParamType);

        final var outputParamType = compileType(inputParamType1);
        return new Some<>(outputParamType + " " + paramName);
    }

    private static String compileType(String input) {
        if (input.equals("var")) return "auto";

        if (input.endsWith("[]")) return "Slice<" + input.substring(0, input.length() - "[]".length()) + ">";

        return compileGenericType(input)
                .or(() -> compileSymbol(input))
                .orElseGet(() -> invalidate("type", input));
    }

    private static Option<String> compileGenericType(String input) {
        final var genStart = input.indexOf("<");
        if (genStart == -1) return new None<>();

        final var caller = input.substring(0, genStart);
        final var withEnd = input.substring(genStart + 1);
        if (!withEnd.endsWith(">")) return new None<>();

        final var inputArgs = withEnd.substring(0, withEnd.length() - ">".length());
        final var outputArgs = splitAndCompile(Main::splitByValues, Main::compileType, Main::mergeValues, inputArgs);
        return new Some<>(caller + "<" + outputArgs + ">");
    }

    private static List<String> splitByValues(String inputParams) {
        final var inputParamsJavaList = new JavaList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = streamChars(inputParams).collect(JavaLinkedList.collector());

        while (!queue.isEmpty()) {
            final var c = popOrPanic(queue);
            if (c == ',' && depth == 0) {
                advance(inputParamsJavaList, buffer);
                buffer = new StringBuilder();
            } else {
                buffer.append(c);
                if (c == '-') {
                    if (!queue.isEmpty() && queue.peek().filter(value -> value == '>').isPresent()) {
                        buffer.append(popOrPanic(queue));
                    }
                }
                if (c == '<' || c == '(') depth++;
                if (c == '>' || c == ')') depth--;
            }
        }
        advance(inputParamsJavaList, buffer);
        return inputParamsJavaList;
    }

    private static Set<Path> filterPaths(Set<Path> paths) {
        return paths.stream()
                .filter(Path::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(JavaSet.collector());
    }
}
