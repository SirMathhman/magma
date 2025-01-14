package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
                final var relative = SOURCE_DIRECTORY.relativize(source);
                System.out.println("Compiling source: " + relative);

                final var parent = relative.getParent();
                final var namespace = new ArrayList<String>();
                for (int i = 0; i < parent.getNameCount(); i++) {
                    namespace.add(parent.getName(i).toString());
                }

                final var parentDirectory = TARGET_DIRECTORY.resolve(parent);

                if (!Files.exists(parentDirectory)) Files.createDirectories(parentDirectory);

                final var name = source.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));

                final var header = parentDirectory.resolve(nameWithoutExt + ".h");
                final var copy = new ArrayList<String>(namespace);
                copy.add(nameWithoutExt + "_h");
                final var joined = String.join("_", copy);
                Files.writeString(header, "#ifndef " + joined + "\n#define " + joined + "\n#endif");

                final var target = parentDirectory.resolve(nameWithoutExt + ".c");
                Files.writeString(target, "#include \"./Main.h\"");
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
