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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        try {
            final var sources = collectSources();
            runWithSources(sources);
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static Set<Path> collectSources() throws IOException {
        try (Stream<Path> stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
        }
    }

    private static void runWithSources(Set<Path> sources) throws IOException, CompileException {
        for (Path source : sources) {
            compileSource(source);
        }
    }

    private static void compileSource(Path source) throws IOException, CompileException {
        final var relative = Main.SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var namespace = computeNamespace(parent);
        final var targetParent = resolveTargetParent(namespace);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = relative.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));

        final var input = Files.readString(source);
        final var output = compileRoot(input, namespace);
        write(targetParent, nameWithoutExt, output);
    }

    private static void write(Path directory, String name, String output) throws IOException {
        final var extensions = List.of(".c", ".h");
        for (String extension : extensions) {
            final var target = directory.resolve(name + extension);
            Files.writeString(target, output);
        }
    }

    private static Path resolveTargetParent(List<String> namespace) {
        var targetParent = TARGET_DIRECTORY;
        for (String s : namespace) {
            targetParent = targetParent.resolve(s);
        }
        return targetParent;
    }

    private static List<String> computeNamespace(Path parent) {
        final var namespace = new ArrayList<String>();
        for (int i = 0; i < parent.getNameCount(); i++) {
            namespace.add(parent.getName(i).toString());
        }
        return namespace;
    }

    private static String compileRoot(String root, List<String> namespace) throws CompileException {
        return compileRootSegments(split(root), namespace);
    }

    private static String compileRootSegments(List<String> segments, List<String> namespace) throws CompileException {
        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(Results.unwrap(compileRootSegment(segment.strip(), namespace)));
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

    private static Result<String, CompileException> compileRootSegment(String rootSegment, List<String> namespace) {
        final var compilers = List.<Supplier<Optional<String>>>of(
                () -> compilePackage(rootSegment),
                () -> compileImport(rootSegment, namespace),
                () -> compileToStruct(rootSegment, "class "),
                () -> compileToStruct(rootSegment, "record "),
                () -> compileToStruct(rootSegment, "interface ")
        );

        return compilers.stream()
                .map(Supplier::get)
                .flatMap(Optional::stream)
                .findFirst()
                .<Result<String, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException("Invalid root segment", rootSegment)));
    }

    private static Optional<String> compileImport(String rootSegment, List<String> namespace) {
        if (!rootSegment.startsWith("import ")) return Optional.empty();
        final var substring = rootSegment.substring("import ".length());

        if (!substring.endsWith(";")) return Optional.empty();
        final var importNamespaceArray = substring.substring(0, substring.length() - 1).split("\\.");
        final var importNamespace = List.of(importNamespaceArray);

        final var rootPath = "../".repeat(namespace.size());
        final var headerPath = rootPath + String.join("/", importNamespace);
        return Optional.of("#include \"" + headerPath + ".h\"\n");
    }

    private static Optional<String> compilePackage(String rootSegment) {
        if (!rootSegment.startsWith("package ")) return Optional.empty();
        return Optional.of("");
    }

    private static Optional<String> compileToStruct(String rootSegment, String infix) {
        return split(rootSegment, infix).flatMap(tuple0 -> {
            return split(tuple0.right(), "{").map(tuple1 -> {
                var name = tuple1.left().strip();
                return "struct " + name + " {\n};";
            });
        });
    }

    private static Optional<Tuple<String, String>> split(String input, String infix) {
        final var index = input.indexOf(infix);
        if (index == -1) return Optional.empty();

        final var before = input.substring(0, index);
        final var after = input.substring(index + infix.length());
        return Optional.of(new Tuple<>(before, after));
    }
}
