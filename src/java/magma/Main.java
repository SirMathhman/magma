package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            runWithSources(sources);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void runWithSources(Set<Path> sources) throws IOException {
        for (Path source : sources) {
            final var sourceParent = Main.SOURCE_DIRECTORY.relativize(source.getParent());
            final var targetParent = TARGET_DIRECTORY.resolve(sourceParent);
            if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

            final var name = source.getFileName().toString();
            final var slice = name.substring(0, name.indexOf('.'));

            final var target = targetParent.resolve(slice + ".mgs");
            final var input = Files.readString(source);
            Files.writeString(target, compile(input));
        }
    }

    private static String compile(String input) {
        return splitAndCompile(input, Main::compileRootSegment);
    }

    private static String splitAndCompile(String input, Function<String, String> mapper) {
        return split(input)
                .stream()
                .map(mapper)
                .reduce(new StringBuilder(), StringBuilder::append, (_, next) -> next)
                .toString();
    }

    private static ArrayList<String> split(String input) {
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

    private static String compileRootSegment(String segment) {
        final var stripped = segment.strip();
        return compilePackage(stripped)
                .or(() -> compileImport(stripped))
                .or(() -> compileClass(stripped))
                .orElse(segment);
    }

    private static Optional<String> compileClass(String input) {
        final var classIndex = input.indexOf("class");
        if (classIndex == -1) return Optional.empty();

        final var beforeKeyword = input.substring(0, classIndex);
        var modifiers = beforeKeyword.equals("public ") ? "export " : "";

        final var afterKeyword = input.substring(classIndex + "class".length());
        final var contentStart = afterKeyword.indexOf('{');
        if (contentStart == -1) return Optional.empty();

        final var name = afterKeyword.substring(0, contentStart).strip();
        final var withEnd = afterKeyword.substring(contentStart + 1).strip();
        if (!withEnd.endsWith("}")) return Optional.empty();
        final var oldContent = withEnd.substring(0, withEnd.length() - "}".length());
        final var newContent = splitAndCompile(oldContent, Main::compileClassMember);

        return Optional.of(modifiers + "class def " + name + "() => {\n" + newContent + "}");
    }

    private static String compileClassMember(String input) {
        return compileDeclaration(input).orElse(input);
    }

    private static Optional<String> compileDeclaration(String input) {
        final var index = input.indexOf('=');
        if (index == -1) return Optional.empty();

        final var header = input.substring(0, index).strip();
        final var withEnd = input.substring(index + 1).strip();
        if (!withEnd.endsWith(";")) return Optional.empty();
        final var value = withEnd.substring(0, withEnd.length() - 1);

        final var last = header.lastIndexOf(" ");
        if (last == -1) return Optional.empty();

        final var withType = header.substring(0, last).strip();
        final var separator = withType.lastIndexOf(' ');
        if (separator == -1) return Optional.empty();
        final var oldModifiers = Arrays.stream(withType.substring(0, separator).strip().split(" ")).toList();
        final var newModifiers = new ArrayList<String>(oldModifiers);
        newModifiers.remove("public");
        newModifiers.remove("final");
        newModifiers.add("let");

        final var type = withType.substring(separator + 1).strip();

        final var name = header.substring(last + 1).strip();
        return Optional.of("\t" + String.join(" ", newModifiers) + " " + name + " : " + type + " = " + value + ";\n");
    }

    private static Optional<String> compileImport(String input) {
        if (input.startsWith("import ")) {
            return Optional.of(input + "\n");
        } else {
            return Optional.empty();
        }
    }

    private static Optional<String> compilePackage(String input) {
        return input.startsWith("package ") ? Optional.of("") : Optional.empty();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
