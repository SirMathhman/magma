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

            var paths = new ArrayList<Path>();
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

                final var input = Files.readString(source);
                final var content = input.contains("class") ? "struct Temp {};" : "";

                final var copy = new ArrayList<>(namespace);
                copy.add(nameWithoutExt + "_h");
                final var joined = String.join("_", copy);
                final var output = "#ifndef " + joined + "\n#define " + joined + "\n" + content + "\n#endif";

                final var header = parentDirectory.resolve(nameWithoutExt + ".h");
                Files.writeString(header, output);
                paths.add(header);

                final var target = parentDirectory.resolve(nameWithoutExt + ".c");
                Files.writeString(target, "#include \"./Main.h\"\nint main(){\n\treturn 0;\n}");
                paths.add(target);
            }

            final var build = TARGET_DIRECTORY.resolve("CMakeLists.txt");
            final var joined = paths.stream()
                    .map(TARGET_DIRECTORY::relativize)
                    .map(Path::toString)
                    .map(path -> path.replaceAll("\\\\", "/"))
                    .map(path -> "./" + path)
                    .collect(Collectors.joining(" "));

            Files.writeString(build, "cmake_minimum_required(VERSION 3.10)\n" +
                                     "\n" +
                                     "project(Magma C)\n" +
                                     "\n" +
                                     "set(CMAKE_C_COMPILER clang)\n" +
                                     "\n" +
                                     "add_executable(Magma " + joined + ")\n");
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }
}
