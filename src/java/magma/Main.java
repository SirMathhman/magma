package magma;

import magma.collect.Deque;
import magma.collect.List;
import magma.collect.Set;
import magma.io.Error;
import magma.io.Path;
import magma.java.JavaLinkedList;
import magma.java.JavaList;
import magma.java.JavaPaths;
import magma.java.JavaSet;
import magma.java.Strings;
import magma.option.None;
import magma.option.Option;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.split.Splitter;
import magma.split.StatementSplitter;
import magma.split.ValueSplitter;
import magma.stream.ArrayHead;
import magma.stream.Collectors;
import magma.stream.HeadedStream;
import magma.stream.LengthHead;
import magma.stream.Stream;
import magma.stream.Streams;

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
        return splitAndCompile(root, new StatementSplitter(), Main::compileRootMember);
    }

    private static String splitAndCompile(
            String input, Splitter splitter,
            Function<String, String> compiler
    ) {
        return splitter.split(input)
                .stream()
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .<Option<StringBuilder>>foldLeft(new None<>(), (output, stripped) -> compileAndMerge(splitter, compiler, output, stripped))
                .map(StringBuilder::toString)
                .orElse("");
    }

    private static Option<StringBuilder> compileAndMerge(Splitter splitter, Function<String, String> compiler, Option<StringBuilder> output, String stripped) {
        final var compiled = compiler.apply(stripped);
        if (output.isEmpty()) {
            return new Some<>(new StringBuilder(compiled));
        } else {
            return output.map(inner -> splitter.merge(inner, compiled));
        }
    }

    private static String compileRootMember(String rootSegment) {
        if (rootSegment.startsWith("package ")) return "";
        if (rootSegment.startsWith("import ")) return rootSegment + "\n";

        return compileToStruct("class", rootSegment)
                .or(() -> compileToStruct("interface", rootSegment))
                .or(() -> compileToStruct("record", rootSegment))
                .orElseGet(() -> invalidate(new CompileError("Unknown " + "root segment", rootSegment)));
    }

    private static Option<String> compileToStruct(String keyword, String rootSegment) {
        final var classIndex = rootSegment.indexOf(keyword);
        if (classIndex == -1) return new None<>();

        final var withoutKeyword = rootSegment.substring(classIndex + keyword.length());
        final var contentStartIndex = withoutKeyword.indexOf("{");
        if (contentStartIndex == -1) return new None<>();

        final var name = withoutKeyword.substring(0, contentStartIndex).strip();
        final var content = withoutKeyword.substring(contentStartIndex + 1, withoutKeyword.length() - 1);
        final var compiled = splitAndCompile(content, new StatementSplitter(), Main::compileClassSegment);
        return new Some<>("struct " + name + " {" + compiled + "\n}");
    }

    private static String invalidate(CompileError error) {
        System.err.println(error.display());
        return error.context();
    }

    private static String compileClassSegment(String classSegment) {
        if (classSegment.endsWith(";")) {
            final var substring = classSegment.substring(0, classSegment.length() - 1);
            final var index = substring.indexOf('=');
            if (index != -1) {
                final var definition = substring.substring(0, index);
                final var compiled = compileValue(2, substring.substring(index + 1));
                return "\n\t" + compileDefinition(definition).orElseGet(() -> invalidate(new CompileError("Unknown " + "definition", definition))) + " = " + compiled + ";";
            }
        }

        return compileMethod(classSegment).match(value -> {
            return value;
        }, err -> {
            return invalidate(new CompileError("class segment", classSegment, err));
        });
    }

    private static Result<String, CompileError> compileMethod(String classSegment) {
        Locator locator3 = new FirstLocator("(");
        return split(classSegment, locator3).flatMapValue(tuple -> {
            final var beforeParamStart = tuple.left();
            final var afterParamStart = tuple.right();

            Locator locator2 = new LastLocator(" ");
            return split(beforeParamStart, locator2).flatMapValue(tuple2 -> {
                final var beforeName = tuple2.left();
                final var name = tuple2.right();

                Locator locator1 = new LastLocator(" ");
                return split(beforeName, locator1).flatMapValue(tuple1 -> {
                    final var type = compileType(tuple1.right());
                    Locator locator = new FirstLocator(")");
                    return split(afterParamStart, locator).flatMapValue(tuple0 -> {
                        final var inputParams = tuple0.left();
                        final var outputParams = splitAndCompile(inputParams, new ValueSplitter(), value -> compileDefinition(value)
                                .orElseGet(() -> invalidate(new CompileError("Unknown " + "definition", value))));

                        final var afterParams = tuple0.right().strip();
                        return truncateLeft(afterParams, "{").flatMapValue(withEnd -> {
                            return truncateRight(withEnd, "}").mapValue(content -> {
                                final var outputContent = splitAndCompile(content, new StatementSplitter(), statement -> compileStatement(statement, 2));
                                return "\n\t" + type + " " + name + "(" + outputParams + "){" + outputContent + "\n\t}";
                            });
                        });
                    });
                });
            });
        });
    }

    private static Result<String, CompileError> truncateLeft(String input, String prefix) {
        if (input.startsWith(prefix)) return new Ok<>(input.substring(prefix.length()));
        return new Err<>(new CompileError("Prefix '" + prefix + "' not present", input));
    }

    private static String compileStatement(String statement, int depth) {
        if (statement.strip().equals("continue;")) return generateStatement(depth, "continue");
        if (statement.strip().equals("break;")) return generateStatement(depth, "break");

        if (statement.startsWith("else")) {
            final var substring = statement.substring("else".length()).strip();
            final String output;
            if (substring.startsWith("{") && substring.endsWith("}")) {
                final var substring1 = substring.substring(1, substring.length() - 1);
                output = splitAndCompile(substring1, new StatementSplitter(), statement0 -> compileStatement(statement0, depth + 1));
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

        final var depth1 = compileInitialization(statement, depth);
        if (depth1.isPresent()) return depth1.unwrap();

        if (statement.endsWith(";")) {
            final var newCaller = compileInvocation(depth, statement.substring(0, statement.length() - ";".length()));
            if (newCaller.isPresent()) return generateStatement(depth, newCaller.unwrap());
        }

        return compileDefinitionStatement(statement)
                .or(() -> compilePostfix(statement, "--", depth))
                .or(() -> compilePostfix(statement, "++", depth))
                .orElseGet(() -> invalidate(new CompileError("Unknown " + "statement", statement)));

    }

    private static Option<String> compileInitialization(String statement, int depth) {
        Locator locator = new FirstLocator("=");
        return split(statement, locator).findValue().flatMap(tuple -> {
            final var beforeEquals = tuple.left();
            final var afterEquals = tuple.right();

            return truncateRight(afterEquals, ";").findValue()
                    .map(String::strip)
                    .flatMap(stripped -> compileDefinition(beforeEquals).map(definition -> {
                        final var compiled1 = compileValue(depth, stripped);
                        return generateStatement(depth, definition + " = " + compiled1);
                    }));
        });
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
                outputContent = splitAndCompile(substring, new StatementSplitter(), statement1 -> compileStatement(statement1, depth + 1));
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
            final var popped = queue.popOrPanic();
            final var i = popped.left();
            final var c = popped.right();

            if (c == '\'') {
                final var popped1 = queue.popOrPanic().right();
                if (popped1 == '\\') {
                    queue.popOrPanic();
                }

                queue.popOrPanic();
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var next = queue.popOrPanic().right();
                    if (next == '"') break;
                    if (next == '\\') queue.popOrPanic();
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
            final var compiled = splitAndCompile(
                    substring1, new ValueSplitter(), value -> compileValue(depth, value.strip())
            );

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
            final var tuple = queue.popOrPanic();
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
            final var popped = queue.popOrPanic();
            if (popped.right() == '\\') {
                queue.popOrPanic();
            }
            queue.popOrPanic();
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
                .orElseGet(() -> invalidate(new CompileError("Unknown " + "value", input)));
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
            compiled = splitAndCompile(substring1, new StatementSplitter(), statement -> compileStatement(statement, depth));
        } else {
            compiled = generateReturn(compileValue(depth, afterArrow), depth + 1);
        }

        return maybeNames.map(names -> {
            final var joinedNames = names.stream()
                    .map(name -> "auto " + name)
                    .collect(Collectors.joining(", "))
                    .orElse("");

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
            final var compiled = splitAndCompile(substring1, new ValueSplitter(), value -> compileValue(depth, value.strip()));

            return compiled1 + "(" + compiled + ")";
        });
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

        return Strings.streamChars(value1)
                .collect(Collectors.allMatch(Character::isDigit));
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
        return compileVar(input)
                .or(() -> compileArray(input))
                .or(() -> compileGenericType(input))
                .or(() -> compileSymbol(input))
                .orElseGet(() -> invalidate(new CompileError("Unknown " + "type", input)));
    }

    private static Option<String> compileVar(String input) {
        return input.equals("var") ? new Some<>("auto") : new None<>();
    }

    private static Option<String> compileArray(String input) {
        return truncateRight(input, "[]").findValue().map(inner -> generateGeneric("Slice", compileType(inner)));
    }

    private static Option<String> compileGenericType(String input) {
        return split(input, new FirstLocator("<")).findValue().flatMap(tuple -> {
            final var caller = tuple.left();
            final var withEnd = tuple.right();
            return truncateRight(withEnd, ">").findValue().map(inputArgs -> {
                final var outputArgs = splitAndCompile(inputArgs, new ValueSplitter(), Main::compileType);
                return generateGeneric(caller, outputArgs);
            });
        });
    }

    private static String generateGeneric(String caller, String outputArgs) {
        return caller + "<" + outputArgs + ">";
    }

    private static Result<Tuple<String, String>, CompileError> split(String input, Locator locator) {
        return locator.locate(input).match(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.sliceLength());
            return new Ok<>(new Tuple<>(left, right));
        }, () -> {
            final var format = "Infix '%s' not present";
            final var message = format.formatted(locator.infix());
            return new Err<>(new CompileError(message, input));
        });
    }

    private static Result<String, CompileError> truncateRight(String input, String suffix) {
        if (input.endsWith(suffix)) return new Ok<>(input.substring(0, input.length() - suffix.length()));
        return new Err<>(new CompileError("Suffix '" + suffix + "' not present", input));
    }

    private static Set<Path> filterPaths(Set<Path> paths) {
        return paths.stream()
                .filter(Path::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(JavaSet.collector());
    }
}
