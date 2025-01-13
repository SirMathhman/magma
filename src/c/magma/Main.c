#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Main {
	int value = 0;
	int value = 0;
	void temp(){try (final var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".java"))
                    .collect(Collectors.toSet());

            for (Path source : sources) {
                final var relativized = SOURCE_DIRECTORY.relativize(source);
                final var parent = relativized.getParent();
                final var name = relativized.getFileName().toString();
                final var nameWithoutExt = name.substring(0, name.indexOf('.'));
                final var targetParent = TARGET_DIRECTORY.resolve(parent);
                if (!Files.exists(targetParent)) Files.createDirectories(targetParent);

                final var target = targetParent.resolve(nameWithoutExt + ".c");
                final var input = Files.readString(source);
                Files.writeString(target, compileRoot(input));
            }
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private static String compileRoot(String root) {
        return splitAndCompile(root, Main::compileRootSegment);}

    private static String splitAndCompile(String root, Function<String, String> compiler) {
        var segments = new ArrayList<String>();var buffer = new StringBuilder();var depth = 0;for (int i = 0;i < root.length();i++) {
            var c = root.charAt(i);
            buffer.append(c);
            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
            }
        }
        advance(buffer, segments);final var output = new StringBuilder();for (String segment : segments) {
            output.append(compiler.apply(segment.strip()));
        }

        return output.toString();}

    private static String compileRootSegment(String rootSegment) {
        if (rootSegment.startsWith("package")) return "";if (rootSegment.startsWith("import")) return "#include \"temp.h\"\n";return compileToStruct("class", rootSegment)
                .or(() -> compileToStruct("record", rootSegment))
                .orElseGet(() -> invalidate("root segment", rootSegment));}

    private static String invalidate(String type, String rootSegment) {
        System.err.println("Invalid " + type + ": " + rootSegment);return rootSegment;}

    private static Optional<String> compileToStruct(String keyword, String rootSegment) {
        return split(rootSegment, keyword).flatMap(tuple -> {
            return split(tuple.right(), "{").flatMap(tuple0 -> {
                return truncateRight(tuple0.right().strip(), "}").map(content -> {
                    final var outputContent = splitAndCompile(content, Main::compileStructSegment);
                    return "struct " + tuple0.left().strip() + " {" + outputContent + "\n};";
                });
            });
        });}

    private static String compileStructSegment(String structSegment) {
        return compileInitialization(structSegment)
                .or(() -> compileMethod(structSegment))
                .orElseGet(() -> invalidate("struct segment", structSegment));}

    private static Optional<String> compileMethod(String structSegment) {
        return truncateRight(structSegment, "}")
                .flatMap(inner -> split(inner, "{")
                        .map(tuple -> {
                            final var inputContent = tuple.right();
                            final var outputContent = splitAndCompile(inputContent, Main::compileStatement);
                            return "\n\tvoid temp(){" + outputContent + "\n\t}";
                        }));}

    private static String compileStatement(String statement) {
        return invalidate("statement", statement);}

    private static Optional<String> compileInitialization(String structSegment) {
        return truncateRight(structSegment, ";").flatMap(inner -> {
            return split(inner, "=").map(value -> "\n\tint value = 0;");
        });}

    private static Optional<String> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) return Optional.of(input.substring(0, input.length() - slice.length()));return Optional.empty();}

    private static Optional<Tuple<String, String>> split(String input, String slice) {
        final var index = input.indexOf(slice);if (index == -1) return Optional.empty();final var left = input.substring(0, index);final var right = input.substring(index + slice.length());return Optional.of(new Tuple<>(left, right));}

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
	}
};