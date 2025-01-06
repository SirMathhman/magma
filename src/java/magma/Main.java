package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");

    public static void main(String[] args) {
        collect()
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(value -> value, Optional::of)
                .ifPresent(applicationError -> System.err.println(applicationError.display()));
    }

    private static Result<List<Path>, IOException> collect() {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .toList();

            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<ApplicationError> runWithSources(List<Path> sources) {
        for (Path source : sources) {
            final var error = runWithSource(source);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Path source) {
        final var relativized = Main.SOURCE_DIRECTORY.relativize(source);
        final var parent = relativized.getParent();
        final var targetDirectory = Paths.get(".", "src", "c");
        final var targetParent = targetDirectory.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoryCreationError = createDirectoriesSafe(targetParent);
            if (directoryCreationError.isPresent()) {
                return directoryCreationError
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }
        }

        final var name = relativized.getFileName().toString();
        final var separator = name.indexOf('.');
        final var nameWithoutExt = name.substring(0, separator);

        return readSafe(source)
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(input -> compileInputToTarget(input, targetParent, nameWithoutExt))
                .match(value -> value, Optional::of);
    }

    private static Optional<ApplicationError> compileInputToTarget(String input, Path targetParent, String nameWithoutExt) {
        return compileRoot(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeOutputToTarget(targetParent, nameWithoutExt, output))
                .match(value -> value, Optional::of);
    }

    private static Optional<ApplicationError> writeOutputToTarget(Path targetParent, String nameWithoutExt, String output) {
        final var target = targetParent.resolve(nameWithoutExt + ".c");
        return writeSafe(output, target)
                .map(JavaError::new)
                .map(ApplicationError::new);
    }

    private static Optional<IOException> writeSafe(String output, Path target) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, IOException> readSafe(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<IOException> createDirectoriesSafe(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return compileSegments(root, Main::compileRootSegment);
    }

    private static Result<String, CompileError> compileSegments(String root, Function<String, Result<String, CompileError>> mapper) {
        return split(root).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
            for (String segment : segments) {
                final var stripped = segment.strip();
                if (!stripped.isEmpty()) {
                    output = output
                            .and(() -> mapper.apply(stripped))
                            .mapValue(tuple -> tuple.left().append(tuple.right()));
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

        if (c == ';' && appended.isLevel()) return appended.advance();
        if (c == '}' && appended.isShallow()) return appended.exit().advance();
        if (c == '{') return appended.enter();
        if (c == '}') return appended.exit();
        return appended;
    }

    private static Result<String, CompileError> compileRootSegment(String segment) {
        return compilePackage(segment)
                .or(() -> compileImport(segment))
                .or(() -> compileToStruct(segment, "class "))
                .or(() -> compileToStruct(segment, "record "))
                .or(() -> compileToStruct(segment, "interface "))
                .orElseGet(() -> new Err<>(new CompileError("Unknown root segment", segment)));
    }

    private static Optional<? extends Result<String, CompileError>> compileToStruct(String segment, String keyword) {
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

        return Optional.of(outputResult.mapValue(output -> "struct " + name + " {" + output + "\n};"));
    }

    private static Result<String, CompileError> compileStructMember(String structMember, String name) {
        return compileDefinition(structMember)
                .or(() -> compileMethod(name, structMember))
                .orElseGet(() -> new Err<>(new CompileError("Unknown struct member", structMember)));
    }

    private static Optional<Result<String, CompileError>> compileDefinition(String structMember) {
        if (!structMember.endsWith(";")) return Optional.empty();

        final var slice = structMember.substring(0, structMember.length() - 1);
        final var space = slice.lastIndexOf(' ');
        if (space == -1) return Optional.empty();

        final var before = slice.substring(0, space);
        final var i = before.lastIndexOf(' ');
        final var type = before.substring(i + 1);

        final var name = slice.substring(space + 1);
        return Optional.of(new Ok<>("\n\t" + type + " " + name + ";"));
    }

    private static Optional<Result<String, CompileError>> compileMethod(String structName, String structMember) {
        final var paramStart = structMember.indexOf("(");
        if (paramStart == -1) return Optional.empty();

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
            final String params;
            final String body;
            if (methodName.equals(structName)) {
                actualName = "new";
                params = "";
                body = "\n\t\tstruct " + structName + " this;" +
                        output +
                        "\n\t\treturn this;";
            } else {
                actualName = methodName;
                params = "void* __ref__";
                final var s = "struct " + structName;
                body = "\n\t\t" + s + "* this = (" + s + "*) __ref__;" + output;
            }

            return "\n\tvoid " + actualName + "(" + params + "){" +
                    body +
                    "\n\t}";
        }));
    }

    private static Result<String, CompileError> compileStatement(String statement) {
        if (statement.contains("=")) return new Ok<>("\n\t\tdestination = source;");
        if (statement.startsWith("return ")) return new Ok<>("\n\t\treturn value;");
        if (statement.contains("(") && statement.endsWith(");")) return new Ok<>("\n\t\tcaller();");
        return new Err<>(new CompileError("Unknown statement", statement));
    }

    private static Optional<? extends Result<String, CompileError>> compileImport(String segment) {
        return segment.startsWith("import ") ? Optional.of(new Ok<>("#include \"temp.h\";\n")) : Optional.empty();
    }

    private static Optional<Result<String, CompileError>> compilePackage(String segment) {
        return segment.startsWith("package ") ? Optional.of(new Ok<>("")) : Optional.empty();
    }
}
