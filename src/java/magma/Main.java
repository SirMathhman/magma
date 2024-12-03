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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final var sourceDirectory = Paths.get(".", "src", "java");
        try (Stream<Path> stream = Files.walk(sourceDirectory)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            runWithSources(sources);
        } catch (IOException | CompileException e) {
            throw new RuntimeException(e);
        }
    }

    private static void runWithSources(Set<Path> sources) throws IOException, CompileException {
        for (Path source : sources) {
            final var fileName = source.getFileName().toString();
            final var separator = fileName.indexOf('.');
            final var name = fileName.substring(0, separator);
            final var target = source.resolveSibling(name + ".mgs");

            final var input = Files.readString(source);
            final var output = compile(input);
            Files.writeString(target, output);
        }
    }

    private static String compile(String input) throws CompileException {
        final var segments = split(input);

        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(Results.unwrap(compileRootMember(segment)));
        }

        return output.toString();
    }

    private static List<String> split(String input) {
        var state = new State();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            var appended = state.append(c);
            state = splitAtChar(appended, c);
        }

        return state.advance().segments();
    }

    private static State splitAtChar(State state, char c) {
        if (c == ';' && state.isLevel()) return state.advance();
        if (c == '{') return state.enter();
        if (c == '}') return state.exit();
        return state;
    }

    private static Result<String, CompileException> compileRootMember(String input) {
        if (input.startsWith("package ")) {
            if (input.endsWith(";")) {
                return new Ok<>("");
            }
        }

        return new Err<>(new CompileException(input));
    }
}
