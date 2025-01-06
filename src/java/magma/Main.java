package magma;

import jv.JavaFiles;
import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.JavaError;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        collect()
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(value -> value, Optional::of)
                .ifPresent(applicationError -> System.err.println(applicationError.display()));
    }

    private static Result<Set<Path>, IOException> collect() {
        return JavaFiles.walkSafe(SOURCE_DIRECTORY).mapValue(Main::filterPaths);
    }

    private static Set<Path> filterPaths(Set<Path> paths) {
        return paths.stream()
                .filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".java"))
                .collect(Collectors.toSet());
    }

    private static Optional<ApplicationError> runWithSources(Set<Path> sourceSet) {
        for (var source : sourceSet) {
            final var relativized = Main.SOURCE_DIRECTORY.relativize(source);
            final var namespace = computeNamespace(relativized.getParent());
            if (!namespace.isEmpty() && namespace.getFirst().equals("jv")) continue;

            final var name = computeName(relativized);
            final var error = runWithSource(source, namespace, name);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Path source, List<String> namespace, String name) {
        final var targetParent = resolveTargetParent(namespace);

        if (!Files.exists(targetParent)) {
            final var directoryCreationError = JavaFiles.createDirectoriesSafe(targetParent);
            if (directoryCreationError.isPresent()) {
                return directoryCreationError
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }
        }

        return JavaFiles.readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> compileInputToTarget(input, targetParent, name))
                .match(value -> value, Optional::of);
    }

    private static Path resolveTargetParent(List<String> namespace) {
        var targetParent = TARGET_DIRECTORY;
        for (String segment : namespace) {
            targetParent = targetParent.resolve(segment);
        }
        return targetParent;
    }

    private static String computeName(Path relativized) {
        final var name = relativized.getFileName().toString();
        final var separator = name.indexOf('.');
        return name.substring(0, separator);
    }

    private static ArrayList<String> computeNamespace(Path parent) {
        final var namespace = new ArrayList<String>();
        for (int i = 0; i < parent.getNameCount(); i++) {
            namespace.add(parent.getName(i).toString());
        }
        return namespace;
    }

    private static Optional<ApplicationError> compileInputToTarget(String input, Path targetParent, String nameWithoutExt) {
        return compileRoot(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeOutputToTarget(targetParent, nameWithoutExt, output))
                .match(value -> value, Optional::of);
    }

    private static Optional<ApplicationError> writeOutputToTarget(Path targetParent, String nameWithoutExt, String output) {
        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return JavaFiles.writeSafe(output, target)
                .map(JavaError::new)
                .map(ApplicationError::new);
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return compileSegments(root, Main::compileRootSegment);
    }

    private static Result<String, CompileError> compileSegments(String root, Function<String, Result<Node, CompileError>> mapper) {
        return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
            for (String segment : segments) {
                final var stripped = segment.strip();
                if (!stripped.isEmpty()) {
                    output = output
                            .and(() -> mapper.apply(stripped))
                            .mapValue(tuple -> tuple.left().append(tuple.right().value()));
                }
            }

            return output.mapValue(StringBuilder::toString);
        });
    }

    private static Result<List<String>, CompileError> split(String input) {
        var state = new State();

        var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            state = splitAtChar(state, c, queue);
        }

        if (state.isLevel()) {
            return new Ok<>(state.advance().segments);
        } else {
            return new Err<>(new CompileError("Invalid depth '" + state.depth + "'", input));
        }
    }

    private static State splitAtChar(State state, char c, Deque<Character> queue) {
        final var appended = state.append(c);
        if (c == '\'') {
            final var maybeEscape = queue.pop();
            final var withMaybeEscape = appended.append(maybeEscape);
            State next;
            if (maybeEscape == '\\') {
                next = withMaybeEscape.append(queue.pop());
            } else {
                next = withMaybeEscape;
            }

            return next.append(queue.pop());
        }

        if (c == '\"') {
            var current = appended;
            while (!queue.isEmpty()) {
                final var next = queue.pop();
                current = current.append(next);

                if (next == '\"') {
                    break;
                }
            }
            return current;
        }

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{' || c == '(') return appended.enter();
        if (c == '}' || c == ')') return appended.exit();
        return appended;
    }

    private static Result<Node, CompileError> compileRootSegment(String segment) {
        return compilePackage(segment)
                .or(() -> compileImport(segment))
                .or(() -> compileToStruct(segment, "class "))
                .or(() -> compileToStruct(segment, "record "))
                .or(() -> compileToStruct(segment, "interface "))
                .orElseGet(() -> new Err<>(new CompileError("Unknown root segment", segment)));
    }

    private static Optional<Result<Node, CompileError>> compileToStruct(String segment, String keyword) {
        final var keywordIndex = segment.indexOf(keyword);
        if (keywordIndex == -1) return Optional.empty();

        final var contentStart = segment.indexOf('{', keywordIndex + keyword.length());
        if (contentStart == -1) return Optional.empty();

        final var contentEnd = segment.lastIndexOf('}');
        if (contentEnd == -1) return Optional.empty();

        final var maybeImplements = segment.substring(keywordIndex + keyword.length(), contentStart);
        String name;
        final var implementsIndex = maybeImplements.indexOf("implements ");
        if (implementsIndex != -1) {
            name = maybeImplements.substring(0, implementsIndex).strip();
        } else {
            name = maybeImplements;
        }

        final var content = segment.substring(contentStart + 1, contentEnd);
        final var outputResult = compileSegments(content, structMember -> compileStructMember(structMember, name));

        return Optional.of(outputResult.mapValue(output -> "struct " + name + " {" + output + "\n};").mapValue(Node::new));
    }

    private static Result<Node, CompileError> compileStructMember(String structMember, String name) {
        return compileDefinition(structMember)
                .or(() -> compileMethod(name, structMember))
                .orElseGet(() -> new Err<>(new CompileError("Unknown struct member", structMember)));
    }

    private static Optional<Result<Node, CompileError>> compileDefinition(String structMember) {
        if (!structMember.endsWith(";")) return Optional.empty();

        final var slice = structMember.substring(0, structMember.length() - 1);
        final var space = slice.lastIndexOf(' ');
        if (space == -1) return Optional.empty();

        final var before = slice.substring(0, space);
        final var i = before.lastIndexOf(' ');
        final var type = before.substring(i + 1);

        final var name = slice.substring(space + 1);
        return Optional.of(new Ok<>(new Node("\n\t" + type + " " + name + ";")));
    }

    private static Optional<Result<Node, CompileError>> compileMethod(String structName, String structMember) {
        final var paramStart = structMember.indexOf("(");
        if (paramStart == -1) return Optional.empty();

        final var paramEnd = structMember.indexOf(')');
        if (paramEnd == -1) return Optional.empty();

        var params = Arrays.stream(structMember.substring(paramStart + 1, paramEnd).split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList();

        final var before = structMember.substring(0, paramStart);
        final var i = before.lastIndexOf(' ');
        final var methodName = before.substring(i + 1);

        final var contentStart = structMember.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var contentEnd = structMember.lastIndexOf('}');
        if (contentEnd == -1) return Optional.empty();

        var content = structMember.substring(contentStart + 1, contentEnd);
        return Optional.of(compileSegments(content, Main::compileStatement).mapValue(output -> {
            final String actualName;
            final List<String> outputParams;
            final String body;
            if (methodName.equals(structName)) {
                actualName = "new";
                outputParams = params;
                body = "\n\t\tstruct " + structName + " this;" +
                        output +
                        "\n\t\treturn this;";
            } else {
                actualName = methodName;
                final var copy = new ArrayList<String>();
                copy.add("void* __ref__");
                copy.addAll(params);

                outputParams = copy;
                final var s = "struct " + structName;
                body = "\n\t\t" + s + " this = *(" + s + "*) __ref__;" + output;
            }

            return "\n\tvoid " + actualName + "(" + String.join(", ", outputParams) + "){" +
                    body +
                    "\n\t}";
        }).mapValue(Node::new));
    }

    private static Result<Node, CompileError> compileStatement(String statement) {
        List<Supplier<Optional<Result<Node, CompileError>>>> list = List.of(
                () -> compileAssignment(statement),
                () -> compileReturn(statement),
                () -> compileInvocationStatement(statement),
                () -> compileConditional("if ", statement),
                () -> compileConditional("while ", statement),
                () -> compileElse(statement),
                () -> compileDefinition(statement),
                () -> compileFor(statement),
                () -> compilePostfix(statement, "++"),
                () -> compilePostfix(statement, "--")
        );

        var errors = new ArrayList<CompileError>();
        for (Supplier<Optional<Result<Node, CompileError>>> supplier : list) {
            final var optional = supplier.get();
            if (optional.isPresent()) {
                final var result = optional.get();
                if (result.isOk()) {
                    return result;
                } else {
                    errors.add(result.findError().orElseThrow());
                }
            }
        }

        return new Err<>(new CompileError("Invalid statement", statement, errors));
    }

    private static Optional<Result<Node, CompileError>> compilePostfix(String statement, String operator) {
        return statement.endsWith(operator + ";") ? Optional.of(new Ok<>(new Node(statement))) : Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileFor(String statement) {
        if (statement.startsWith("for ")) return Optional.of(new Ok<>(new Node("\n\t\tfor(;;) {}")));
        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileElse(String statement) {
        if (statement.startsWith("else ")) return Optional.of(new Ok<>(new Node("\n\t\telse {}")));
        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileConditional(String prefix, String statement) {
        if (statement.startsWith(prefix)) return Optional.of(new Ok<>(new Node("\n\t\t" + prefix + "(1) {}")));
        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileInvocationStatement(String statement) {
        if (statement.contains("(") && statement.endsWith(");"))
            return Optional.of(new Ok<>(new Node("\n\t\tcaller();")));
        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileReturn(String statement) {
        if (!statement.startsWith("return ")) return Optional.empty();

        final var substring = statement.substring("return ".length());
        if (!substring.endsWith(";")) return Optional.empty();

        final var substring1 = substring.substring(0, substring.length() - ";".length());
        return Optional.of(compileValue(substring1).mapValue(value -> "\n\t\treturn " + value.value() + ";").mapValue(Node::new));
    }

    private static Optional<Result<Node, CompileError>> compileAssignment(String statement) {
        if (!statement.endsWith(";")) return Optional.empty();
        final var slice = statement.substring(0, statement.length() - ";".length());
        final var separator = slice.indexOf("=");

        if (separator == -1) return Optional.empty();
        final var destination = slice.substring(0, separator).strip();
        final var source = slice.substring(separator + 1).strip();
        return Optional.of(compileValue(source)
                .mapValue(value -> "\n\t\t" + destination + " = " + value.value() + ";")
                .mapValue(Node::new));
    }

    private static Result<Node, CompileError> compileValue(String value) {
        return compileSymbol(value)
                .or(() -> compileConstruction(value))
                .or(() -> compileInvocation(value))
                .or(() -> compileDataAccess(value))
                .or(() -> compileChar(value))
                .or(() -> compileOperator(value, "+"))
                .or(() -> compileOperator(value, "&&"))
                .or(() -> compileFunctionAccess(value))
                .map(result -> result.mapErr(err -> new CompileError("Invalid value", value, err)))
                .orElseGet(() -> new Err<>(new CompileError("Unknown value", value)));
    }

    private static Optional<Result<Node, CompileError>> compileFunctionAccess(String value) {
        return value.contains("::") ? Optional.of(new Ok<>(new Node("obj::property"))) : Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileOperator(String value, String operator) {
        return value.contains(operator) ? Optional.of(new Ok<>(new Node("a " + operator + " b"))) : Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileConstruction(String value) {
        return value.startsWith("new ") ? Optional.of(new Ok<>(new Node("temp()"))) : Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileChar(String value) {
        if (value.startsWith("'") && value.endsWith("'")) return Optional.of(new Ok<>(new Node(value)));
        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileDataAccess(String value) {
        final var separator = value.indexOf('.');
        if (separator == -1) return Optional.empty();

        final var object = value.substring(0, separator);
        final var property = value.substring(separator + 1);
        return Optional.of(compileValue(object)
                .mapValue(obj -> obj.value() + "." + property)
                .mapValue(value1 -> new Node("data-access", value1)));
    }

    private static Optional<Result<Node, CompileError>> compileInvocation(String value) {
        if (!value.endsWith(")")) return Optional.empty();
        final var slice = value.substring(0, value.length() - 1);

        final var argumentsStart = findArgumentsStart(slice);
        if (argumentsStart.isEmpty()) return Optional.empty();

        final var caller = slice.substring(0, argumentsStart.get());
        final var compiled = compileValue(caller);

        final var substring = slice.substring(argumentsStart.get() + 1);
        final var result = compileValue(substring);

        return Optional.of(compiled.and(() -> result).mapValue(tuple -> {
            final var leftNode = tuple.left();
            final var leftValue = leftNode.value();
            final var rightValue = tuple.right().value();

            final String content;
            if (leftNode.is("data-access")) {
                final var arguments = rightValue.isEmpty() ? "__caller__" : "__caller__, " + rightValue;
                content = "{\n\t\t\tvoid __caller__ = " + leftValue + ";\n\t\t\t__caller__(" + arguments + ")\n\t\t}";
            } else {
                content = leftValue + "(" + rightValue + ")";
            }
            return new Node(content);
        }));
    }

    private static Optional<Integer> findArgumentsStart(String slice) {
        var depth = 0;
        for (int i = slice.length() - 1; i >= 0; i--) {
            var c = slice.charAt(i);
            if (c == '(' && depth == 0) return Optional.of(i);
            if (c == ')') depth++;
            if (c == '(') depth--;
        }
        return Optional.empty();
    }

    private static Optional<Result<Node, CompileError>> compileSymbol(String value) {
        for (int i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            if (Character.isLetter(c) || c == '_') continue;
            return Optional.empty();
        }

        return Optional.of(new Ok<>(new Node(value)));
    }

    private static Optional<Result<Node, CompileError>> compileImport(String segment) {
        if (!segment.startsWith("import ")) return Optional.empty();
        final var substring = segment.substring("import ".length());

        if(!substring.endsWith(";")) return Optional.empty();
        final var joined = String.join("/", substring.substring(0, substring.length() - ";".length())
                .split("\\."));

        return Optional.of(new Ok<>(new Node("#include \"" +
                joined +
                ".h\";\n")));
    }

    private static Optional<Result<Node, CompileError>> compilePackage(String segment) {
        return segment.startsWith("package ") ? Optional.of(new Ok<>(new Node(""))) : Optional.empty();
    }
}