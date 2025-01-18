package magma;

import magma.split.Splitter;
import magma.split.StatementSplitter;
import magma.split.ValueSplitter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                compileSource(source);
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static void compileSource(Path source) throws IOException {
        final var relativized = SOURCE_DIRECTORY.relativize(source);
        final var parent = relativized.getParent();
        final var nameWithExt = relativized.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var input = Files.readString(source);
        final var output = compile(input).orElse("");

        final var target = targetParent.resolve(name + ".c");
        Files.writeString(target, output);

        final var header = targetParent.resolve(name + ".h");
        Files.writeString(header, output);
    }

    private static Optional<String> compile(String root) {
        return compileStatements(root, Main::compileRootSegment);
    }

    private static Optional<String> compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package ")) return Optional.of("");
        if (rootSegment.startsWith("import ")) return Optional.of("#include \"temp.h\"\n");
        final var keyword = rootSegment.indexOf("class");
        if (keyword != -1) {
            final var afterKeyword = rootSegment.substring(keyword + "class".length());
            final var contentStart = afterKeyword.indexOf('{');
            if (contentStart != -1) {
                final var name = afterKeyword.substring(0, contentStart).strip();
                final var withEnd = afterKeyword.substring(contentStart + 1).strip();
                if (withEnd.endsWith("}")) {
                    final var content = withEnd.substring(0, withEnd.length() - 1);
                    final var maybeOutputContent = compileStatements(content, Main::compileStructSegment);
                    if (maybeOutputContent.isPresent()) {
                        return Optional.of("struct " + name + " {" + maybeOutputContent.get() + "\n};");
                    }
                }
            }
        }
        return invalidate("root segment", rootSegment);
    }

    private static Optional<String> compileStatements(String content, Function<String, Optional<String>> compiler) {
        return getString(content, compiler, new StatementSplitter());
    }

    private static Optional<String> getString(String content, Function<String, Optional<String>> compiler, Splitter splitter) {
        List<String> segments = splitter.split(content);
        var output = new StringBuilder();
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            final var optional = compiler.apply(stripped);
            if (optional.isEmpty()) return Optional.empty();
            output = splitter.merge(output, optional.get());
        }

        return Optional.of(output.toString());
    }

    private static Optional<String> compileStructSegment(String structSegment) {
        return compileInitialization(structSegment)
                .or(() -> compileMethod(structSegment))
                .or(() -> invalidate("struct segment", structSegment));
    }

    private static Optional<String> compileMethod(String structSegment) {
        final var index = structSegment.indexOf('(');
        if (index != -1) {
            final var inputDefinition = structSegment.substring(0, index).strip();
            return compileDefinition(inputDefinition).map(outputDefinition -> "\n\t" + outputDefinition + "(){\n\t}");
        }

        return Optional.empty();
    }

    private static Optional<String> compileInitialization(String structSegment) {
        final var index = structSegment.indexOf("=");
        if (index == -1) return Optional.empty();

        final var definition = structSegment.substring(0, index).strip();
        final var stripped = structSegment.substring(index + 1).strip();
        if (!stripped.endsWith(";")) return Optional.empty();

        final var inputValue = stripped.substring(0, stripped.length() - 1);
        return compileValue(inputValue).flatMap(outputValue -> compileDefinition(definition).map(outputDefinition -> "\n\t" + outputDefinition + " = " + outputValue + ";"));
    }

    private static Optional<String> compileValue(String value) {
        return compileInvocation(value)
                .or(() -> compileDataAccess(value))
                .or(() -> compileSymbol(value))
                .or(() -> compileString(value))
                .or(() -> invalidate("value", value));
    }

    private static Optional<String> compileString(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) return Optional.of(value);
        else return Optional.empty();
    }

    private static Optional<String> compileDataAccess(String value) {
        final var index = value.lastIndexOf('.');
        if (index == -1) return Optional.empty();

        final var stripped = value.substring(0, index).strip();
        final var optional = compileValue(stripped);
        final var stripped1 = value.substring(index + 1).strip();
        return optional.map(s -> s + "." + stripped1);
    }

    private static Optional<String> compileInvocation(String value) {
        final var index = value.indexOf("(");
        if (index == -1) return Optional.empty();

        final var inputValue = value.substring(0, index).strip();
        final var stripped = value.substring(index + 1).strip();
        if (!stripped.endsWith(")")) return Optional.empty();

        final var substring = stripped.substring(0, stripped.length() - 1);
        return splitAndCompileValues(substring, Main::compileValue)
                .flatMap(arguments -> compileValue(inputValue).map(outputString -> outputString + "(" + arguments + ")"));
    }

    private static Optional<String> splitAndCompileValues(String substring, Function<String, Optional<String>> compiler) {
        return getString(substring, compiler, new ValueSplitter());
    }

    private static Optional<String> compileDefinition(String definition) {
        final var index1 = definition.lastIndexOf(' ');
        if (index1 == -1) return Optional.empty();

        final var beforeName = definition.substring(0, index1).strip();
        final Optional<String> maybeOutputType;
        final var index2 = beforeName.lastIndexOf(' ');
        if (index2 == -1) {
            maybeOutputType = compileType(beforeName);
        } else {
            maybeOutputType = compileType(beforeName.substring(index2 + 1));
        }

        if (maybeOutputType.isEmpty()) return Optional.empty();
        final var outputType = maybeOutputType.get();
        final var name = definition.substring(index1 + 1).strip();
        return Optional.of(outputType + " " + name);
    }

    private static Optional<String> compileType(String type) {
        return compileSymbol(type)
                .or(() -> compileGeneric(type))
                .or(() -> invalidate("type", type));
    }

    private static Optional<String> compileSymbol(String type) {
        if (isSymbol(type)) return Optional.of(type);
        return Optional.empty();
    }

    private static Optional<String> compileGeneric(String type) {
        final var index = type.indexOf('<');
        if (index == -1) return Optional.empty();

        final var caller = type.substring(0, index).strip();
        final var stripped = type.substring(index + 1).strip();
        if (!stripped.endsWith(">")) return Optional.empty();

        final var substring = stripped.substring(0, stripped.length() - 1);
        return splitAndCompileValues(substring, Main::compileType).map(compiled -> caller + "<" + compiled + ">");
    }

    private static boolean isSymbol(String type) {
        for (int i = 0; i < type.length(); i++) {
            final var c = type.charAt(i);
            if (Character.isLetter(c)) continue;
            return false;
        }

        return true;
    }

    private static Optional<String> invalidate(String type, String input) {
        System.err.println("Invalid " + type + ": " + input);
        return Optional.empty();
    }
}
