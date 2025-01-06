package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.result.Results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
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
        try {
            run();
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void run() throws IOException, CompileException {
        final var sources = collect();
        for (Path source : sources) {
            runWithSource(source);
        }
    }

    private static Set<Path> collect() throws IOException {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        }
    }

    private static void runWithSource(Path source) throws IOException, CompileException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var relativeParent = relativized.getParent();
        final var relativeName = relativized.getFileName().toString();
        final var separator = relativeName.indexOf('.');
        final var name = relativeName.substring(0, separator);

        final var targetParent = TARGET_DIRECTORY.resolve(relativeParent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var target = targetParent.resolve(name + ".c");
        final var input = Files.readString(source);
        final var output = Results.unwrap(splitAndCompile(input, Main::compileRootSegment));

        Files.writeString(target, output);
    }

    private static Result<String, CompileException> splitAndCompile(String root, Function<String, Result<String, CompileException>> mapper) {
        return split(root).flatMapValue(segments -> compileSegments(segments, mapper));
    }

    private static Result<String, CompileException> compileSegments(List<String> segments, Function<String, Result<String, CompileException>> mapper) {
        Result<StringBuilder, CompileException> output = new Ok<>(new StringBuilder());
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (!stripped.isEmpty()) {
                output = output.and(() -> mapper.apply(stripped))
                        .mapValue(tuple -> tuple.left().append(tuple.right()));
            }
        }

        return output.mapValue(StringBuilder::toString);
    }

    private static Result<List<String>, CompileException> split(String root) {
        var state = new State();

        final var queue = IntStream.range(0, root.length())
                .mapToObj(root::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = splitAtChar(state, c, queue);
        }

        if (state.isLevel()) {
            return new Ok<>(state.advance().segments);
        } else {
            return new Err<>(new CompileException("Invalid depth '" + state.depth + "'", root));
        }
    }

    private static State splitAtChar(State state, char c, Deque<Character> queue) {
        final var appended = state.append(c);
        if (c == '\'') {
            final var maybeEscape = queue.pop();
            final var withMaybeEscape = state.append(maybeEscape);
            State withNext;
            if (maybeEscape == '\\') {
                withNext = withMaybeEscape.append(queue.pop());
            } else {
                withNext = withMaybeEscape;
            }

            return withNext.append(queue.pop());
        }

        if (c == '"') {
            var current = state;
            while (!queue.isEmpty()) {
                final var next = queue.pop();
                current = current.append(next);
                if (next == '\"') break;
                if (next == '\\') {
                    current = current.append(queue.pop());
                }
            }
            return current;
        }

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static Result<String, CompileException> compileRootSegment(String rootSegment) {
        return compilePackage(rootSegment)
                .or(() -> compileImport(rootSegment))
                .or(() -> compileToStruct("class ", rootSegment))
                .or(() -> compileRecord(rootSegment))
                .or(() -> compileToStruct("interface ", rootSegment))
                .orElseGet(() -> new Err<>(new CompileException("Invalid root segment", rootSegment)));
    }

    private static Optional<Result<String, CompileException>> compileRecord(String rootSegment) {
        final var index = rootSegment.indexOf("record ");
        if (index == -1) return Optional.empty();

        final var afterKeyword = rootSegment.substring(index + "record ".length());
        final var paramStart = afterKeyword.indexOf('(');
        if (paramStart == -1) return Optional.empty();

        final var name = afterKeyword.substring(0, paramStart).strip();
        final var afterParamStart = afterKeyword.substring(paramStart + 1);

        final var paramEnd = afterParamStart.indexOf(')');
        if (paramEnd == -1) return Optional.empty();

        final var params = afterParamStart.substring(0, paramEnd);
        final var state = splitByValues(params);
        final var parameters = state.advance().segments;

        final var afterParams = afterParamStart.substring(paramEnd + 1);
        final var contentStart = afterParams.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var implementsType = afterParams.substring(0, contentStart).strip();

        final var afterContent = afterParams.substring(contentStart + 1);
        if (!afterContent.endsWith("}")) return Optional.empty();
        final var content = afterContent.substring(0, afterContent.length() - "}".length());
        return Optional.of(splitAndCompile(content, structSegment -> compileStructSegment(name, structSegment)).mapValue(output -> {
            final var joinedParameters = parameters.stream()
                    .map(value -> "\n\t" + value + ";")
                    .collect(Collectors.joining());

            final String impl;
            if (implementsType.startsWith("implements ")) {
                impl = "\n\timpl " + implementsType.substring("implements ".length()) + " {" +
                        output +
                        "\n\t}";
            } else {
                impl = output;
            }

            return "struct " + name + " {" + joinedParameters + impl + "\n};";
        }));
    }

    private static Result<String, CompileException> compileStructSegment(String name, String structSegment) {
        return compileMethod(structSegment, name)
                .orElseGet(() -> new Err<>(new CompileException("Unknown struct segment", structSegment)));
    }

    private static Optional<Result<String, CompileException>> compileMethod(String structSegment, String structName) {
        final var paramStart = structSegment.indexOf('(');
        if (paramStart == -1) return Optional.empty();

        final var beforeParams = structSegment.substring(0, paramStart);
        final var nameSeparator = beforeParams.lastIndexOf(' ');

        final var substring = beforeParams.substring(0, nameSeparator);
        final var maybeTypeStart = getI(substring);
        if (maybeTypeStart.isEmpty()) return Optional.empty();

        final var type = substring.substring(maybeTypeStart.get() + 1);

        final var methodName = beforeParams.substring(nameSeparator + 1).strip();

        final var afterParamStart = structSegment.substring(paramStart + 1);
        final var contentStart = afterParamStart.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var withParamEnd = afterParamStart.substring(0, contentStart).strip();
        if (!withParamEnd.endsWith(")")) return Optional.empty();

        final var paramsArray = withParamEnd.substring(0, withParamEnd.length() - ")".length()).split(",");
        final var paramsList = Arrays.stream(paramsArray)
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList();

        final var params = new ArrayList<String>();
        params.add("void* __ref__");
        params.addAll(paramsList);

        final var afterContentStart = afterParamStart.substring(contentStart + 1).strip();
        if (!afterContentStart.endsWith("}")) return Optional.empty();
        final var content = afterContentStart.substring(0, afterContentStart.length() - 1);

        final var structType = "struct " + structName;
        return Optional.of(splitAndCompile(content, Main::compileStatement).mapValue(output -> {
            return "\n\t\t" + type + " " + methodName + "(" +
                    String.join(", ", params) +
                    "){\n\t\t\t" + structType + " this = *(" + structType + "*) __ref__;" +
                    output +
                    "\n\t\t}";
        }));
    }

    private static Optional<Integer> getI(String slice) {
        var depth = 0;
        for (int i = slice.length() - 1; i >= 0; i--) {
            var c = slice.charAt(i);
            if (c == ' ' && depth == 0) {
                return Optional.of(i);
            } else {
                if (c == '>') depth++;
                if (c == '<') depth--;
            }
        }

        return Optional.empty();
    }

    private static Result<String, CompileException> compileStatement(String statement) {
        return compileReturn(statement)
                .orElseGet(() -> new Err<>(new CompileException("Unknown statement", statement)));
    }

    private static Optional<Result<String, CompileException>> compileReturn(String statement) {
        if (!statement.startsWith("return ")) return Optional.empty();

        final var withEnd = statement.substring("return ".length());
        if (!withEnd.endsWith(";")) return Optional.empty();

        final var value = withEnd.substring(0, withEnd.length() - 1);
        return Optional.of(compileValue(value).mapValue(output -> {
            return "\n\t\t\treturn " + output + ";";
        }));
    }

    private static Result<String, CompileException> compileValue(String value) {
        return compileInvocation(value).orElseGet(() -> new Err<>(new CompileException("Unknown value", value)));
    }

    private static Optional<Result<String, CompileException>> compileInvocation(String value) {
        if (value.contains("(") && value.endsWith(")")) return Optional.of(new Ok<>("temp()"));
        return Optional.empty();
    }

    private static State splitByValues(String params) {
        var state = new State();
        for (int i = 0; i < params.length(); i++) {
            final var c = params.charAt(i);
            if (c == ';') {
                state = state.advance();
            } else {
                state = state.append(c);
            }
        }
        return state;
    }

    private static Optional<Result<String, CompileException>> compileToStruct(String infix, String rootSegment) {
        final var index = rootSegment.indexOf(infix);
        if (index == -1) return Optional.empty();

        final var after = rootSegment.substring(index + infix.length());
        final var nameEnd = after.indexOf('{');
        if (nameEnd == -1) return Optional.empty();

        final var name = after.substring(0, nameEnd).strip();
        return Optional.of(new Ok<>("struct " + name + " {\n};"));
    }

    private static Optional<Result<String, CompileException>> compileImport(String rootSegment) {
        if (rootSegment.startsWith("import ")) return Optional.of(new Ok<>("#include \"temp.h\"\n"));
        return Optional.empty();
    }

    private static Optional<Result<String, CompileException>> compilePackage(String rootSegment) {
        if (rootSegment.startsWith("package ")) return Optional.of(new Ok<>(""));
        return Optional.empty();
    }
}
