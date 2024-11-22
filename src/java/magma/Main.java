package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            compileSources(stream);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void compileSources(Stream<Path> stream) throws IOException, CompileException {
        final var files = stream
                .filter(file -> file.getFileName().toString().endsWith(".java"))
                .collect(Collectors.toSet());

        for (Path sourceFile : files) {
            compileSource(sourceFile);
        }
    }

    private static void compileSource(Path source) throws IOException, CompileException {
        final var sourceRelativized = SOURCE_DIRECTORY.relativize(source);
        final var sourceNamespace = findNamespace(sourceRelativized);

        final var targetParent = sourceNamespace.stream().reduce(TARGET_DIRECTORY, Path::resolve, (_, next) -> next);

        final var nameWithExtension = source.getFileName().toString();
        final var name = nameWithExtension.substring(0, nameWithExtension.indexOf('.'));

        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);
        final var target = targetParent.resolve(name + ".mgs");

        final var input = Files.readString(source);
        Files.writeString(target, compile(input));
    }

    private static String compile(String input) throws CompileException {
        return JavaResults.unwrap(splitAndParse(input, segment -> compileRootMember(segment.strip())));
    }

    private static List<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
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

    private static Result<String, CompileException> compileRootMember(String input) {
        return compilePackage(input)
                .or(() -> compileImport(input))
                .or(() -> compileClass(input))
                .orElseGet(() -> new Err<>(new CompileException("Unknown input", input)));
    }

    private static Optional<Result<String, CompileException>> compileClass(String input) {
        final var keywordIndex = input.indexOf("class");
        if (keywordIndex == -1) return Optional.empty();

        final var modifierSlice = input.substring(0, keywordIndex);
        final var oldModifiers = computeModifiers(modifierSlice);

        final var afterKeyword = input.substring(keywordIndex + "class".length()).strip();
        final var contentStart = afterKeyword.indexOf('{');
        final var name = afterKeyword.substring(0, contentStart).strip();
        final var bodyWithEnd = afterKeyword.substring(contentStart + 1);
        if (!bodyWithEnd.endsWith("}")) return Optional.empty();
        final var body = bodyWithEnd.substring(0, bodyWithEnd.length() - 1).strip();
        return Optional.of(splitAndParse(body, Main::compileClassMember).mapValue(buffer -> {
            final var newModifiers = passModifiers(oldModifiers);
            final var modifierString = computeNewModifierString(newModifiers);
            return modifierString + "class def " + name + "() => {" + buffer + "}";
        }));
    }

    private static Result<String, CompileException> splitAndParse(String input, Function<String, Result<String, CompileException>> mapper) {
        return split(input)
                .stream()
                .map(mapper)
                .reduce(new Ok<>(new StringBuilder()), (BiFunction<Result<StringBuilder, CompileException>, Result<String, CompileException>, Result<StringBuilder, CompileException>>)
                        (first, second) -> first.and(() -> second).mapValue(tuple -> tuple.left().append(tuple.right())), (_, next) -> next)
                .mapValue(StringBuilder::toString);
    }

    private static Result<String, CompileException> compileClassMember(String classMember) {
        return new Err<>(new CompileException("Invalid class member", classMember));
    }

    private static List<String> computeModifiers(String modifierSlice) {
        final var modifiersArray = modifierSlice
                .strip()
                .split(" ");

        return Arrays.stream(modifiersArray)
                .filter(modifier -> !modifier.isEmpty())
                .toList();
    }

    private static ArrayList<String> passModifiers(List<String> oldModifiers) {
        var newModifiers = new ArrayList<String>();
        for (String oldModifier : oldModifiers) {
            if (oldModifier.equals("public")) {
                newModifiers.add("export");
            }
        }
        return newModifiers;
    }

    private static String computeNewModifierString(ArrayList<String> newModifiers) {
        String modifierString;
        if (newModifiers.isEmpty()) {
            modifierString = "";
        } else {
            modifierString = String.join(" ", newModifiers) + " ";
        }
        return modifierString;
    }

    private static Optional<Result<String, CompileException>> compileImport(String input) {
        return input.startsWith("import ") ? Optional.of(new Ok<>(input + "\n")) : Optional.empty();
    }

    private static Optional<Result<String, CompileException>> compilePackage(String input) {
        return input.startsWith("package ") ? Optional.of(new Ok<>("")) : Optional.empty();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static List<String> findNamespace(Path path) {
        return IntStream.range(0, path.getNameCount() - 1)
                .mapToObj(path::getName)
                .map(Path::toString)
                .toList();
    }
}
