package magma;

import magma.error.ApplicationError;
import magma.error.CompileError;
import magma.error.JavaError;
import magma.locate.FirstLocator;
import magma.locate.LastLocator;
import magma.locate.Locator;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;
import magma.stream.Stream;
import magma.stream.Streams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "c");

    public static void main(String[] args) {
        collect().mapErr(JavaError::new)
                .mapErr(ApplicationError::new)
                .mapValue(Main::runWithSources)
                .match(Function.identity(), Optional::of)
                .ifPresent(error -> System.err.println(error.display()));
    }

    private static Result<Set<Path>, IOException> collect() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            final var sources = stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .collect(Collectors.toSet());
            return new Ok<>(sources);
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<ApplicationError> runWithSources(Set<Path> sources) {
        for (Path source : sources) {
            final var error = runWithSource(source);
            if (error.isPresent()) return error;
        }
        return Optional.empty();
    }

    private static Optional<ApplicationError> runWithSource(Path source) {
        final var relative = SOURCE_DIRECTORY.relativize(source);
        final var parent = relative.getParent();
        final var nameWithExt = relative.getFileName().toString();
        final var name = nameWithExt.substring(0, nameWithExt.indexOf('.'));

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        if (!Files.exists(targetParent)) {
            final var directoriesError = createDirectoriesWrapped(targetParent);
            if (directoriesError.isPresent()) return directoriesError.map(JavaError::new).map(ApplicationError::new);
        }

        return readStringWrapped(source).mapErr(JavaError::new).mapErr(ApplicationError::new).mapValue(input -> {
            return splitAndCompile(input, Main::compileRootSegment).mapErr(ApplicationError::new).mapValue(output -> {
                final var target = targetParent.resolve(name + ".c");
                final var header = targetParent.resolve(name + ".h");
                return writeStringWrapped(target, output)
                        .or(() -> writeStringWrapped(header, output))
                        .map(JavaError::new)
                        .map(ApplicationError::new);
            }).match(Function.identity(), Optional::of);
        }).match(Function.identity(), Optional::of);
    }

    private static Result<String, IOException> readStringWrapped(Path source) {
        try {
            return new Ok<>(Files.readString(source));
        } catch (IOException e) {
            return new Err<>(e);
        }
    }

    private static Optional<IOException> createDirectoriesWrapped(Path targetParent) {
        try {
            Files.createDirectories(targetParent);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Optional<IOException> writeStringWrapped(Path target, String output) {
        try {
            Files.writeString(target, output);
            return Optional.empty();
        } catch (IOException e) {
            return Optional.of(e);
        }
    }

    private static Result<String, CompileError> splitAndCompile(String input, Function<String, Result<String, CompileError>> compiler) {
        return split(input).flatMapValue(segments -> {
            Result<StringBuilder, CompileError> output = new Ok<>(new StringBuilder());
            for (String segment : segments) {
                final var stripped = segment.strip();
                if (stripped.isEmpty()) continue;
                output = output.and(() -> compiler.apply(stripped)).mapValue(tuple -> tuple.left().append(tuple.right()));
            }

            return output.mapValue(StringBuilder::toString);
        });
    }

    private static Result<List<String>, CompileError> split(String input) {
        final var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;

        final var queue = IntStream.range(0, input.length())
                .mapToObj(input::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                final var c1 = queue.pop();
                buffer.append(c1);

                if (c1 == '\\') {
                    buffer.append(queue.pop());
                }
                buffer.append(queue.pop());
                continue;
            }

            if (c == '"') {
                while (!queue.isEmpty()) {
                    final var c1 = queue.pop();
                    buffer.append(c1);

                    if (c1 == '"') break;
                    if (c1 == '\\') {
                        buffer.append(queue.pop());
                    }
                }

                continue;
            }

            if (c == ';' && depth == 0) {
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else if (c == '}' && depth == 1) {
                depth--;
                advance(buffer, segments);
                buffer = new StringBuilder();
            } else {
                if (c == '{' || c == '(') depth++;
                if (c == '}' || c == ')') depth--;
            }
        }
        advance(buffer, segments);

        if (depth == 0) {
            return new Ok<>(segments);
        } else {
            return new Err<>(new CompileError("Invalid depth '" + depth + "'", input));
        }
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    private static Result<String, CompileError> compileRootSegment(String input) {
        return or("root segment", input, Streams.of(
                () -> compileNamespaced(input, "package ", ""),
                () -> compileNamespaced(input, "import ", "#include <temp.h>\n"),
                () -> compileToStruct(input, "class "),
                () -> compileToStruct(input, "record "),
                () -> compileToStruct(input, "interface ")
        ));
    }

    private static Result<String, CompileError> or(String type, String input, Stream<Supplier<Result<String, CompileError>>> stream) {
        return stream.map(Main::prepare)
                .foldLeft(Supplier::get, (current, next) -> current.or(next).mapErr(Main::merge))
                .map(result -> result.mapErr(errors -> new CompileError("Invalid " + type, input, errors)))
                .orElseGet(() -> new Err<>(new CompileError("No compilers present", input)));
    }

    private static Result<String, CompileError> compileNamespaced(String input, String prefix, String output) {
        if (input.startsWith(prefix)) return new Ok<>(output);
        return new Err<>(new CompileError("Prefix '" + prefix + "' not present.", input));
    }

    private static List<CompileError> merge(Tuple<List<CompileError>, List<CompileError>> tuple) {
        final var left = tuple.left();
        final var right = tuple.right();
        final var copy = new ArrayList<>(left);
        copy.addAll(right);
        return copy;
    }

    private static Result<String, CompileError> compileToStruct(String input, String infix) {
        return split(new FirstLocator(infix), input).flatMapValue(tuple -> {
            return split(new FirstLocator("{"), tuple.right()).flatMapValue(tuple0 -> {
                final var beforeContent = tuple0.left().strip();
                return or("root segment", beforeContent, Streams.of(
                        () -> split(new FirstLocator("("), beforeContent).mapValue(Tuple::left),
                        () -> new Ok<>(beforeContent)
                )).flatMapValue(name -> {
                    final var stripped = tuple0.right().strip();
                    return truncateRight(stripped, "}").flatMapValue(content -> {
                        return splitAndCompile(content, structSegment -> compileStructSegment(structSegment, name)).mapValue(outputContent -> {
                            return "struct " + name + " {" + outputContent + "\n};";
                        });
                    });
                });
            });
        });
    }

    private static Result<String, CompileError> compileStructSegment(String structSegment, String structName) {
        return or("struct segment", structSegment, Streams.of(
                () -> compileMethod(structSegment, structName),
                () -> truncateRight(structSegment, ";").flatMapValue(inner -> {
                    return split(new FirstLocator("="), inner).flatMapValue(tuple -> {
                        return compileDefinition(tuple.left()).mapValue(inner0 -> "\n\t\t" + inner0 + " = temp;");
                    });
                }),
                () -> truncateRight(structSegment, ";").flatMapValue(Main::compileDefinition)
        ));
    }

    private static Result<String, CompileError> compileMethod(String structSegment, String structName) {
        return split(new FirstLocator("("), structSegment).flatMapValue(tuple -> {
            return split(new FirstLocator(")"), tuple.right().strip()).flatMapValue(tuple0 -> {
                final var stripped = tuple0.right().strip();
                return or("root segment", stripped, Streams.of(() -> truncateLeft(stripped, "{").flatMapValue(left -> {
                    return truncateRight(left, "}").flatMapValue(content -> {
                        return splitAndCompile(content, Main::compileStatement).mapValue(outputContent -> {
                            return "{" + outputContent + "\n\t}";
                        });
                    });
                }), () -> stripped.equals(";") ? new Ok<>(";") : new Err<>(new CompileError("Exact string ';' was not present", stripped)))).flatMapValue(content -> {
                    return compileDefinition(tuple.left().strip()).mapValue(definition -> {
                        return "\n\t" + definition + "()" + content;
                    });
                });
            });
        });
    }

    private static Result<String, CompileError> compileStatement(String statement) {
        return or("statement segment", statement, Streams.of(
                () -> truncateRight(statement, ");").flatMapValue(inner -> {
                    return split(new FirstLocator("("), inner).flatMapValue(inner0 -> {
                        final var inputCaller = inner0.left();
                        return compileValue(inputCaller).mapValue(outputCaller -> {
                            return generateStatement(outputCaller + "()");
                        });
                    });
                }),
                () -> truncateLeft(statement, "return ").flatMapValue(inner -> {
                    return truncateRight(inner, ";").flatMapValue(inner1 -> {
                        return compileValue(inner1).mapValue(inner0 -> {
                            return generateStatement("return " + inner0);
                        });
                    });
                }),
                () -> split(new FirstLocator(" "), statement).mapValue(inner -> generateStatement("temp = temp")),
                () -> truncateRight(statement, "++;").mapValue(inner -> "temp++;")
        ));
    }

    private static Result<String, CompileError> compileValue(String value) {
        return or("value", value, Streams.of(
                () -> split(new LastLocator("."), value).flatMapValue(tuple -> {
                    return compileValue(tuple.left()).mapValue(inner -> inner + "." + tuple.right());
                }),
                () -> {
                    if (isSymbol(value)) return new Ok<>(value);
                    return new Err<>(new CompileError("Not a symbol", value));
                }
        ));
    }

    private static String generateStatement(String content) {
        return "\n\t\t" + content + ";";
    }

    private static Result<String, CompileError> truncateLeft(String input, String slice) {
        if (input.startsWith(slice)) return new Ok<>(input.substring(slice.length()));
        return new Err<>(new CompileError("Prefix '" + slice + "' not present", input));
    }

    private static Result<String, CompileError> compileDefinition(String definition) {
        return split(new LastLocator(" "), definition).flatMapValue(tuple1 -> {
            final var inputType = tuple1.left().strip();
            return or("root segment", inputType, Streams.of(
                    () -> split(new LastLocator(" "), inputType).mapValue(Tuple::right),
                    () -> new Ok<>(inputType)
            )).flatMapValue(type -> {
                final var name = tuple1.right();
                if (isSymbol(name)) {
                    return new Ok<>(generateDefinition(type, name));
                } else {
                    return new Err<>(new CompileError("Not a symbol", name));
                }
            });
        });
    }

    private static boolean isSymbol(String input) {
        for (int i = 0; i < input.length(); i++) {
            final var c = input.charAt(i);
            if (Character.isLetter(c)) continue;
            return false;
        }
        return true;
    }

    private static String generateDefinition(String type, String name) {
        return type + " " + name;
    }

    private static Result<String, CompileError> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) {
            return new Ok<>(input.substring(0, input.length() - slice.length()));
        } else {
            return new Err<>(new CompileError("Suffix '" + slice + "' not present", input));
        }
    }

    private static Result<Tuple<String, String>, CompileError> split(Locator locator, String input) {
        return locator.locate(input).<Result<Tuple<String, String>, CompileError>>map(index -> {
            final var left = input.substring(0, index);
            final var right = input.substring(index + locator.length());
            final var tuple = new Tuple<>(left, right);
            return new Ok<>(tuple);
        }).orElseGet(() -> new Err<>(new CompileError("Infix '" + locator.unwrap() + "' not present", input)));
    }

    private static Supplier<Result<String, List<CompileError>>> prepare(Supplier<Result<String, CompileError>> supplier) {
        return () -> supplier.get().mapErr(Collections::singletonList);
    }
}
