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
        final var output = compileRoot(input);

        Files.writeString(target, output);
    }

    private static String compileRoot(String root) throws CompileException {
        final var segments = split(root);
        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(Results.unwrap(compileRootSegment(segment.strip())));
        }

        return output.toString();
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
                .<Result<String, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException("Invalid root segment", rootSegment)));
    }

    private static Optional<String> compileRecord(String rootSegment) {
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

        final var joinedParameters = parameters.stream()
                .map(value -> "\n\t" + value)
                .collect(Collectors.joining());

        return Optional.of("struct " + name + " {" + joinedParameters + "\n};");
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

    private static Optional<? extends String> compileToStruct(String infix, String rootSegment) {
        final var index = rootSegment.indexOf(infix);
        if (index == -1) return Optional.empty();

        final var after = rootSegment.substring(index + infix.length());
        final var nameEnd = after.indexOf('{');
        if (nameEnd == -1) return Optional.empty();

        final var name = after.substring(0, nameEnd).strip();
        return Optional.of("struct " + name + " {\n};");
    }

    private static Optional<? extends String> compileImport(String rootSegment) {
        if (rootSegment.startsWith("import ")) return Optional.of("#include \"temp.h\"\n");
        return Optional.empty();
    }

    private static Optional<String> compilePackage(String rootSegment) {
        if (rootSegment.startsWith("package ")) return Optional.of("");
        return Optional.empty();
    }
}
