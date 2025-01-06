package magma;

import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.result.Results;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        final var output = Results.unwrap(compileSegments(input, Main::compileRootSegment));

        Files.writeString(target, output);
    }

    private static Result<String, CompileException> compileSegments(String root, Function<String, Result<String, CompileException>> mapper) {
        final var segments = split(root);
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

    private static List<String> split(String root) {
        var state = new State();
        for (int i = 0; i < root.length(); i++) {
            var c = root.charAt(i);
            state = splitAtChar(state, c);
        }

        return state.advance().segments;
    }

    private static State splitAtChar(State state, char c) {
        final var appended = state.append(c);
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
        final String impl;
        if (implementsType.startsWith("implements ")) {
            impl = "\n\timpl " + implementsType.substring("implements ".length()) + " {\n\t}";
        } else {
            impl = "";
        }

        final var afterContent = afterParams.substring(contentStart + 1);
        if (!afterContent.endsWith("}")) return Optional.empty();
        final var content = afterContent.substring(0, afterContent.length() - "}".length());
        return Optional.of(compileSegments(content, Main::compileStructSegment).mapValue(output -> {
            final var joinedParameters = parameters.stream()
                    .map(value -> "\n\t" + value + ";")
                    .collect(Collectors.joining());

            return "struct " + name + " {" + joinedParameters + impl + output + "\n};";
        }));
    }

    private static Result<String, CompileException> compileStructSegment(String structSegment) {
        return new Err<>(new CompileException("Invalid struct segment", structSegment));
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
