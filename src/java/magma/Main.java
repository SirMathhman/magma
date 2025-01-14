package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                final var content = compile(input);

                final var copy = new ArrayList<>(namespace);
                copy.add(nameWithoutExt + "_h");
                final var joined = String.join("_", copy);
                final var output = "#ifndef " + joined + "\n#define " + joined + "\n" + content + "\n#endif";

                final var header = parentDirectory.resolve(nameWithoutExt + ".h");
                Files.writeString(header, output);
                paths.add(header);

                final var target = parentDirectory.resolve(nameWithoutExt + ".c");
                final var main = (namespace.equals(List.of("magma")) && nameWithoutExt.equals("Main")) ? "int main(){\n\treturn 0;\n}" : "";
                Files.writeString(target, "#include \"./Main.h\"\n" + main);
                paths.add(target);
            }

            final var build = TARGET_DIRECTORY.resolve("CMakeLists.txt");
            final var joined = paths.stream()
                    .map(TARGET_DIRECTORY::relativize)
                    .map(Path::toString)
                    .map(path -> path.replaceAll("\\\\", "/"))
                    .map(path -> "\n\t./" + path)
                    .collect(Collectors.joining());

            Files.writeString(build, "cmake_minimum_required(VERSION 3.10)\n" +
                                     "\n" +
                                     "project(Magma C)\n" +
                                     "\n" +
                                     "set(CMAKE_C_COMPILER clang)\n" +
                                     "\n" +
                                     "add_executable(Magma " + joined + "\n)\n");
        } catch (IOException | CompileException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compile(String input) throws CompileException {
        final var stripped = input.strip();
        return compileToStruct("class", stripped)
                .or(() -> compileToStruct("record", stripped))
                .orElseThrow(() -> new CompileException("Unknown root", stripped));
    }

    private static Optional<String> compileToStruct(String keyword, String input) {
        return split(input, keyword).flatMap(tuple -> {
            return split(tuple.right(), "{").flatMap(tuple1 -> {
                final var name = tuple1.left().strip();
                return truncateRight(tuple1.right(), "}").map(content -> {
                    final var segments = split(content);

                    var output = new StringBuilder();
                    for (String segment : segments) {
                        output.append(compileToStructMember(segment));
                    }

                    return "struct " + name + " {" + output + "\n};";
                });
            });
        });
    }

    private static ArrayList<String> split(String content) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < content.length(); i++) {
            var c = content.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
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

    private static String compileToStructMember(String structMember) {
        System.err.println("Invalid struct member: " + structMember);
        return structMember;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Optional<String> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) return Optional.of(input.substring(0, input.length() - slice.length()));
        return Optional.empty();
    }

    private static Optional<Tuple<String, String>> split(String input, String slice) {
        final var index = input.indexOf(slice);
        if (index == -1) return Optional.empty();

        final var left = input.substring(0, index);
        final var right = input.substring(index + slice.length());
        return Optional.of(new Tuple<>(left, right));
    }
}
