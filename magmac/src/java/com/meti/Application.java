package com.meti;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class Application {
    private final SourceSet sourceSet;
    private final Path targetRoot;
    private final List<String> targetExtensions;

    public Application(SourceSet sourceSet, Path targetRoot, String... targetExtensions) {
        this.sourceSet = sourceSet;
        this.targetRoot = targetRoot;
        this.targetExtensions = List.of(targetExtensions);
    }

    private List<State> compile(String input, String sourceExtension, String targetExtension) {
        var lines = new ArrayList<String>();
        var builder = new StringBuilder();
        var depth = 0;
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            if (c == ';' && depth == 0) {
                lines.add(builder.toString());
                builder = new StringBuilder();
            } else {
                if (c == '{') depth++;
                if (c == '}') depth--;
                builder.append(c);
            }
        }

        lines.add(builder.toString());
        lines.removeIf(String::isBlank);

        if (sourceExtension.equals(".java")) {
            lines.removeIf(line -> line.startsWith("package"));
        }

        var collect = lines.stream()
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(line -> {
                    var compiled = compileImport(line, sourceExtension, targetExtension);
                    if (compiled.isPresent()) return new State(compiled.get(), Optional.empty());

                    if (line.startsWith("export class def ")) {
                        var paramStart = line.indexOf('(');
                        var name = line.substring("export class def ".length(), paramStart).strip();
                        var body = line.substring(line.indexOf('{'), line.lastIndexOf('}') + 1);
                        return new State("\nfunction " + name + "()" + body, Optional.of(name));
                    }

                    if (line.startsWith("public class ")) {
                        var name = line.substring("public class ".length(), line.indexOf('{')).strip();
                        var body = line.substring(line.indexOf('{'), line.lastIndexOf('}') + 1).strip();

                        return new State("\nexport class def " + name + "() => " + body, Optional.empty());
                    }

                    return new State(line, Optional.empty());
                })
                .collect(Collectors.toList());
        return collect;
    }

    private Optional<String> compileImport(String line, String sourceExtension, String targetExtension) {
        if (line.startsWith("import ")) {
            var segmentString = line.substring("import ".length()).strip();

            var childStart = segmentString.indexOf('{');
            var childEnd = segmentString.indexOf('}');

            if (sourceExtension.equals(".java")) {
                var arrays = Arrays.asList(segmentString.split("\\."));
                if (arrays.isEmpty()) return Optional.of(line);
                if (arrays.size() == 1) return Optional.of("import " + arrays.get(0));

                var child = arrays.get(arrays.size() - 1);
                var parent = String.join(".", arrays.subList(0, arrays.size() - 1));
                return Optional.ofNullable("\nimport { %s } from %s;".formatted(child, parent));
            } else if (targetExtension.equals(".js")) {
                var child = segmentString.substring(childStart + 1, childEnd).strip();
                var fromIndex = segmentString.indexOf("from ", childEnd);
                if (fromIndex == -1) return Optional.of(line);

                var parent = segmentString.substring(fromIndex + "from ".length()).strip();
                return Optional.of("\nconst { " + child + " } = require(\"" + parent + "\");");
            } else if (targetExtension.equals(".c")) {
                return Optional.of("");
            } else if (targetExtension.equals(".h")) {
                var fromIndex = segmentString.indexOf("from ", childEnd);
                if (fromIndex == -1) return Optional.of(line);

                var parent = segmentString.substring(fromIndex + "from ".length()).strip();
                return Optional.of("\n#include <" + parent + ".h>");
            } else {
                return Optional.of(line);
            }
        }
        return Optional.empty();
    }

    void run() throws IOException {
        var set = this.sourceSet().collect();

        for (var path : set) {
            var input = path.read();
            var package_ = path.findPackage();
            var name = path.findName();

            for (String targetExtension : targetExtensions) {
                var target = package_.stream()
                        .reduce(targetRoot, Path::resolve, (path1, path2) -> path2)
                        .resolve(name + targetExtension);

                var parent = target.getParent();
                if (!Files.exists(parent)) Files.createDirectories(parent);

                var results = compile(input, sourceSet.findExtension(), targetExtension);
                var main = results.stream().map(state -> state.value).collect(Collectors.joining());

                var exports = results.stream()
                        .map(state -> state.exports)
                        .flatMap(Optional::stream)
                        .toList();

                var joinedExports = exports.stream()
                        .map(value -> "\n\t" + value)
                        .collect(Collectors.joining(""));

                String exportString;
                if (joinedExports.isEmpty()) {
                    exportString = "";
                } else {
                    exportString = "\nmodule.exports = {" + joinedExports + "\n};";
                }

                String output = main + exportString;
                Files.writeString(target, output);
            }
        }
    }

    public SourceSet sourceSet() {
        return sourceSet;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Application) obj;
        return Objects.equals(this.sourceSet, that.sourceSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceSet);
    }

    @Override
    public String toString() {
        return "Application[" +
               "sourceSet=" + sourceSet + ']';
    }

    record State(String value, Optional<String> exports) {
    }

}