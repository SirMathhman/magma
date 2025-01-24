package magma;

import magma.api.io.Path;
import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;
import magma.api.result.Result;
import magma.api.stream.JoiningCollector;
import magma.api.stream.Streams;
import magma.app.error.ApplicationError;
import magma.app.error.CompileError;
import magma.app.error.JavaError;
import magma.app.lang.CLang;
import magma.app.lang.JavaLang;
import magma.app.pass.CFormatter;
import magma.app.pass.InlinePassUnit;
import magma.app.pass.PassUnit;
import magma.app.pass.RootPasser;
import magma.app.pass.TreePassingStage;
import magma.java.JavaFiles;
import magma.java.JavaList;
import magma.java.JavaListCollector;
import magma.java.JavaPaths;
import magma.java.JavaSet;

import java.io.IOException;
import java.util.function.Function;

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

        System.out.println("Compiling source: " + joined);

        final var targetParent = TARGET_DIRECTORY.resolvePath(parent);
        if (!targetParent.isExists()) {
            final var directoriesError = targetParent.createAsDirectories();
            if (directoriesError.isPresent())
                return directoriesError.map(JavaError::new).map(ApplicationError::new);
        }

        return source.readStrings()
                .mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .flatMapValue(input -> compile(input, namespace).mapErr(ApplicationError::new))
                .mapValue(output -> writeOutput(output, targetParent, name))
                .match(Function.identity(), Some::new);
    }

    private static boolean shouldSkip(JavaList<String> namespace) {
        return namespace.subList(0, 2)
                .filter(slice -> slice.equals(JavaList.of("magma", "java")))
                .isPresent();
    }

    private static Result<String, CompileError> compile(String input, JavaList<String> namespace) {
        return JavaLang.createJavaRootRule().parse(input)
                .flatMapValue(root -> new TreePassingStage(new RootPasser()).pass(new InlinePassUnit<>(root, namespace)).mapValue(PassUnit::value))
                .flatMapValue(root -> new TreePassingStage(new CFormatter()).pass(new InlinePassUnit<>(root, namespace)).mapValue(PassUnit::value))
                .flatMapValue(root -> CLang.createCRootRule().generate(root));
    }

    private static Option<ApplicationError> writeOutput(String output, Path targetParent, String name) {
        final var target = targetParent.resolveChild(name + ".c");
        final var header = targetParent.resolveChild(name + ".h");
        return target.writeString(output)
                .or(() -> header.writeString(output))
                .map(JavaError::new)
                .map(ApplicationError::new);
    }
}
