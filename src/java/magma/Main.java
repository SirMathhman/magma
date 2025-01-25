package magma;

import magma.api.Tuple;
import magma.api.io.Path;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Result;
import magma.api.stream.JoiningCollector;
import magma.api.stream.Streams;
import magma.app.compile.node.Node;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.app.lang.CLang;
import magma.app.compile.pass.CFormatter;
import magma.app.compile.pass.InlinePassUnit;
import magma.app.compile.pass.JavaParser;
import magma.app.compile.pass.PassUnit;
import magma.app.compile.pass.Passer;
import magma.app.compile.pass.RootPasser;
import magma.app.compile.pass.TreePassingStage;
import magma.java.JavaFiles;
import magma.java.JavaList;
import magma.java.JavaListCollector;
import magma.java.JavaPaths;
import magma.java.JavaSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.lang.System.out;
import static magma.app.lang.JavaLang.*;

public class Main {
    public static final Path SOURCE_DIRECTORY = JavaPaths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = JavaPaths.get(".", "src", "c");

    public static void main(String[] args) {
        collect().mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(Function.identity(), Some::new)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Result<JavaSet<Path>, IOException> collect() {
        return SOURCE_DIRECTORY.walkWrapped().mapValue(paths -> paths.stream()
                .filter(JavaFiles::isRegularFile)
                .filter(path -> path.format().endsWith(".java"))
                .collect(JavaSet.collect()));
    }

    private static Option<ApplicationError> runWithSources(JavaSet<Path> sources) {
        return sources.stream()
                .map(Main::runWithSource)
                .flatMap(Streams::fromOption)
                .next();
    }

    private static Option<ApplicationError> runWithSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        return relative.findParent()
                .flatMap(parent -> runWithRelative(source, parent, relative));
    }

    private static Option<ApplicationError> runWithRelative(
            Path source,
            Path parent,
            Path relative
    ) {
        final var namespace = parent.stream()
                .map(Path::format)
                .collect(new JavaListCollector<String>());

        if (shouldSkip(namespace)) {
            return new None<>();
        }

        final var nameWithExt = relative.getFileName().format();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var copy = namespace.add(name);
        final var joined = copy.stream()
                .collect(new JoiningCollector("."))
                .orElse("");

        out.println("Compiling source: " + joined);

        final var targetParent = TARGET_DIRECTORY.resolvePath(parent);
        if (!targetParent.isExists()) {
            final var directoriesError = targetParent.createAsDirectories();
            if (directoriesError.isPresent())
                return directoriesError.map(JavaError::new).map(ApplicationError::new);
        }

        return source.readStrings()
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .flatMapValue(input -> compile(input, namespace, name).mapErr(ApplicationError::new))
                .mapValue(output -> writeOutput(output, targetParent, name))
                .match(Function.identity(), Some::new);
    }

    private static boolean shouldSkip(JavaList<String> namespace) {
        return namespace.subList(0, 2)
                .filter(slice -> slice.equals(JavaList.of("magma", "java")))
                .isPresent();
    }

    private static Result<Map<String, String>, CompileError> compile(String input, JavaList<String> namespace, String name) {
        final var parsed = createJavaRootRule().parse(input);
        return parsed.flatMapValue(root1 -> pass(new JavaParser(), namespace, name, root1))
                .flatMapValue(root -> pass(new RootPasser(), namespace, name, root))
                .flatMapValue(root -> pass(new CFormatter(), namespace, name, root))
                .flatMapValue(root -> root.nodes().stream().foldLeftToResult(new HashMap<>(), Main::generateTarget));
    }

    private static Result<Node, CompileError> pass(
            Passer passer,
            JavaList<String> namespace,
            String name,
            Node root
    ) {
        final var unit = new InlinePassUnit<>(namespace, name, root);
        return new TreePassingStage(passer).pass(unit).mapValue(PassUnit::value);
    }

    private static Result<Map<String, String>, CompileError> generateTarget(Map<String, String> map, Tuple<String, Node> tuple) {
        final var key = tuple.left();
        final var root = tuple.right();

        return CLang.createCRootRule().generate(root).mapValue(generated -> {
            map.put(key, generated);
            return map;
        });
    }

    private static Option<ApplicationError> writeOutput(Map<String, String> output, Path targetParent, String name) {
        final var target = targetParent.resolveChild(name + ".c");
        final var header = targetParent.resolveChild(name + ".h");
        return target.writeString(output.get("source"))
                .or(() -> header.writeString(output.get("header")))
                .map(JavaError::new)
                .map(ApplicationError::new);
    }
}
