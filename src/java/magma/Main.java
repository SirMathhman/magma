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
        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

        final var name = relative.getFileName().toString();
        final var nameWithoutExt = name.substring(0, name.indexOf('.'));
        final var target = targetParent.resolve(nameWithoutExt + ".c");

        final var input = Files.readString(source);
        final var output = compileRoot(input);

        Files.writeString(target, output);
    }

    private static String compileRoot(String root) throws CompileException {
        return compileRootSegments(split(root));
    }

    private static String compileRootSegments(List<String> segments) throws CompileException {
        var output = new StringBuilder();
        for (String segment : segments) {
            output.append(compileRootSegment(segment.strip()));
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

    private static String compileRootSegment(String rootSegment) throws CompileException {
        return Results.unwrap(getString(rootSegment));
    }

    private static Result<String, CompileException> getString(String rootSegment) {
        final var compilers = List.<Supplier<Optional<String>>>of(
                () -> compileNamespaced(rootSegment, "package ", ""),
                () -> compileNamespaced(rootSegment, "import ", "#include \"temp.h\"\n"),
                () -> compileClass(rootSegment, "class "),
                () -> compileClass(rootSegment, "record "),
                () -> compileClass(rootSegment, "interface ")
        );

        return compilers.stream()
                .map(Supplier::get)
                .flatMap(Optional::stream)
                .findFirst()
                .<Result<String, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException("Invalid root segment", rootSegment)));
    }

    private static Optional<String> compileClass(String rootSegment, String infix) {
        if (!rootSegment.contains(infix)) return Optional.empty();
        return Optional.of("struct Temp {};");
    }

    private static Optional<String> compileNamespaced(String rootSegment, String prefix, String value) {
        if (!rootSegment.startsWith(prefix)) return Optional.empty();
        return Optional.of(value);
    }
}
