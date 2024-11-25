package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.JavaError;
import magma.option.None;
import magma.option.Option;
import magma.option.Options;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        extracted().ifPresent(error -> System.err.println(error.display()));
    }

    private static Option<ApplicationError> extracted() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .map(Main::compile)
                    .flatMap(Options::stream)
                    .findFirst()
                    .<Option<ApplicationError>>map(Some::new)
                    .orElseGet(None::new);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> compile(Path sourceFile) {
        return readString(sourceFile)
                .mapValue(input -> compileWithInput(sourceFile, input))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> compileWithInput(Path sourceFile, String input) {
        final var relativized = Main.SOURCE_DIRECTORY.relativize(sourceFile);
        final var parent = relativized.getParent();

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        return createDirectories(targetParent)
                .or(() -> compileWithTargetParent(sourceFile, targetParent, input));
    }

    private static Option<ApplicationError> compileWithTargetParent(Path sourceFile, Path targetParent, String input) {
        final var fileName = sourceFile.getFileName().toString();
        final var separator = fileName.indexOf('.');
        var name = fileName.substring(0, separator);
        final var target = targetParent.resolve(name + ".mgs");

        return compileRoot(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeOutput(target, output))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeOutput(Path file, String output) {
        try {
            Files.writeString(file, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> createDirectories(Path directories) {
        if (Files.exists(directories)) return new None<>();

        try {
            Files.createDirectories(directories);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, ApplicationError> readString(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return split(root)
                .stream()
                .map(String::strip)
                .map(Main::compileRootSegment)
                .<Result<StringBuilder, CompileError>>reduce(new Ok<>(new StringBuilder()),
                        (current, next) -> current.and(() -> next).mapValue(tuple -> tuple.left().append(tuple.right())),
                        (_, next) -> next)
                .mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        final var length = root.length();
        for (int i = 0; i < length; i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static Result<String, CompileError> compileRootSegment(String rootSegment) {
        return compilePackage(rootSegment)
                .or(() -> compileImport(rootSegment))
                .or(() -> compileClass(rootSegment))
                .or(() -> compileRecord(rootSegment))
                .or(() -> compileInterface(rootSegment))
                .orElseGet(() -> new Err<>(new CompileError("Invalid root segment", rootSegment)));
    }

    private static Option<Result<String, CompileError>> compileInterface(String rootSegment) {
        if (rootSegment.contains("interface")) return new Some<>(new Ok<>("trait Temp {}"));
        return new None<>();
    }

    private static Option<Result<String, CompileError>> compileRecord(String rootSegment) {
        final var keywordIndex = rootSegment.indexOf("record ");
        if (keywordIndex == -1) return new None<>();

        final var modifiersString = rootSegment.substring(0, keywordIndex);
        final var newModifiers = parseModifiers(modifiersString);

        final var afterKeyword = rootSegment.substring(keywordIndex + "record ".length());
        final var paramStart = afterKeyword.indexOf('(');
        if (paramStart == -1) return new None<>();

        final var name = afterKeyword.substring(0, paramStart).strip();
        final var afterOpenParentheses = afterKeyword.substring(paramStart + 1).strip();
        final var paramEnd = afterOpenParentheses.indexOf(')');
        if (paramEnd == -1) return new None<>();

        final var params = afterOpenParentheses.substring(0, paramEnd).strip();

        final String paramsOut;
        final var separator = params.indexOf(' ');
        if (separator == -1) {
            paramsOut = "";
        } else {
            final var paramType = params.substring(0, separator).strip();
            final var paramName = params.substring(separator + 1).strip();
            paramsOut = paramName + ": " + paramType;
        }

        final var afterParams = afterOpenParentheses.substring(paramEnd + 1);
        final var contentStart = afterParams.indexOf('{');
        if (contentStart == -1) return new None<>();

        final var maybeImplements = afterParams.substring(0, contentStart).strip();
        String content;
        if (maybeImplements.startsWith("implements ")) {
            final var traitName = maybeImplements.substring("implements ".length());
            content = "\n\timpl " + traitName + ";";
        } else {
            content = "";
        }

        return new Some<>(generateFunction(newModifiers, name, paramsOut, content));
    }

    private static List<String> parseModifiers(String modifiersString) {
        final var modifiersArray = modifiersString.strip().split(" ");
        final var oldModifiers = Arrays.stream(modifiersArray)
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList();

        var newModifiers = new ArrayList<String>();
        if (oldModifiers.contains("public")) {
            newModifiers.add("export");
        }
        newModifiers.add("class");
        return newModifiers;
    }

    private static Option<Result<String, CompileError>> compileClass(String rootSegment) {
        if (rootSegment.contains("class")) {
            return new Some<>(generateFunction(Collections.singletonList("class"), "Temp", "", ""));
        } else {
            return new None<>();
        }
    }

    private static Result<String, CompileError> generateFunction(List<String> modifiers, String name, String params, String content) {
        final var joined = String.join(" ", modifiers);
        return new Ok<>(joined + " def " + name + "(" +
                params +
                ") => {" +
                content +
                "\n}");
    }

    private static Option<Result<String, CompileError>> compileImport(String rootSegment) {
        if (rootSegment.startsWith("import ")) return new Some<>(new Ok<>(rootSegment));
        return new None<>();
    }

    private static Option<Result<String, CompileError>> compilePackage(String rootSegment) {
        return rootSegment.startsWith("package ") ? new Some<>(new Ok<>("")) : new None<>();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
