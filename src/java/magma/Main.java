package magma;

import magma.error.*;
import magma.option.None;
import magma.option.Option;
import magma.option.Options;
import magma.option.Some;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static final Path SOURCE_DIRECTORY = Paths.get(".", "src", "java");
    public static final Path TARGET_DIRECTORY = Paths.get(".", "src", "magma");

    public static void main(String[] args) {
        extracted().ifPresent(error -> System.err.println(error.display()));
    }

    private static Option<ApplicationError> extracted() {
        try (var stream = Files.walk(SOURCE_DIRECTORY)) {
            return stream.filter(file -> file.getFileName().toString().endsWith(".java"))
                    .map(Main::compile)
                    .flatMap(Options::stream)
                    .findFirst()
                    .<Option<ApplicationError>>map(Some::new)
                    .orElseGet(None::new);
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> compile(Path sourceFile) {
        return readString(sourceFile)
                .mapValue(input -> compileWithInput(sourceFile, input))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> compileWithInput(Path sourceFile, String input) {
        final var relativized = Main.SOURCE_DIRECTORY.relativize(sourceFile);
        final var parent = relativized.getParent();

        final var targetParent = TARGET_DIRECTORY.resolve(parent);
        return createDirectories(targetParent)
                .or(() -> compileWithTargetParent(sourceFile, targetParent, input));
    }

    private static Option<ApplicationError> compileWithTargetParent(Path sourceFile, Path targetParent, String input) {
        final var fileName = sourceFile.getFileName().toString();
        final var separator = fileName.indexOf('.');
        var name = fileName.substring(0, separator);
        final var target = targetParent.resolve(name + ".mgs");

        return compileRoot(input)
                .mapErr(ApplicationError::new)
                .mapValue(output -> writeOutput(target, output))
                .match(value -> value, Some::new);
    }

    private static Option<ApplicationError> writeOutput(Path file, String output) {
        try {
            Files.writeString(file, output);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Option<ApplicationError> createDirectories(Path directories) {
        if (Files.exists(directories)) return new None<>();

        try {
            Files.createDirectories(directories);
            return new None<>();
        } catch (IOException e) {
            return new Some<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, ApplicationError> readString(Path path) {
        try {
            return new Ok<>(Files.readString(path));
        } catch (IOException e) {
            return new Err<>(new ApplicationError(new JavaError<>(e)));
        }
    }

    private static Result<String, CompileError> compileRoot(String root) {
        return parseAndCompile(root, Main::compileRootSegment);
    }

    private static Result<String, CompileError> parseAndCompile(String root, Function<String, Result<String, CompileError>> mapper) {
        return split(root)
                .stream()
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(mapper)
                .<Result<StringBuilder, CompileError>>reduce(new Ok<>(new StringBuilder()),
                        (current, next) -> current.and(() -> next).mapValue(tuple -> tuple.left().append(tuple.right())),
                        (_, next) -> next)
                .mapValue(StringBuilder::toString);
    }

    private static ArrayList<String> split(String root) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        var depth = 0;
        final var length = root.length();
        final var queue = IntStream.range(0, length)
                .mapToObj(root::charAt)
                .collect(Collectors.toCollection(LinkedList::new));

        while (!queue.isEmpty()) {
            final var c = queue.pop();
            buffer.append(c);

            if (c == '\'') {
                final var maybeEscape = queue.pop();
                buffer.append(maybeEscape);

                if (maybeEscape == '\\') {
                    buffer.append(queue.pop());
                }

                buffer.append(queue.pop());
                continue;
            }

            if (c == '\"') {
                while (!queue.isEmpty()) {
                    var next = queue.pop();
                    buffer.append(next);
                    if (next == '\"') {
                        break;
                    } else if (next == '\\') {
                        if (!queue.isEmpty()) {
                            buffer.append(queue.pop());
                        }
                    }
                }
            }

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

    private static Result<String, CompileError> compileRootSegment(String rootSegment) {
        return compilePackage(rootSegment)
                .or(() -> compileImport(rootSegment))
                .or(() -> compileClass(rootSegment))
                .or(() -> compileRecord(rootSegment))
                .or(() -> compileInterface(rootSegment))
                .orElseGet(() -> new Err<>(new CompileError("Invalid root segment", new StringContext(rootSegment))));
    }

    private static Option<Result<String, CompileError>> compileInterface(String rootSegment) {
        final var keywordIndex = rootSegment.indexOf("interface");
        if (keywordIndex == -1) return new None<>();

        final var modifiersString = rootSegment.substring(0, keywordIndex).strip();
        final var modifiers = parseModifiers(modifiersString);

        final var afterKeyword = rootSegment.substring(keywordIndex + "interface".length());
        final var contentStart = afterKeyword.indexOf('{');
        if (contentStart == -1) return new None<>();

        final var name = afterKeyword.substring(0, contentStart).strip();
        final var withEnd = afterKeyword.substring(contentStart + 1).strip();
        final var contentEnd = withEnd.lastIndexOf('}');
        if (contentEnd == -1) return new None<>();

        final var content = withEnd.substring(0, contentEnd);
        return new Some<>(parseAndCompile(content, Main::compileInnerMember)
                .mapValue(outputContent -> renderTrait(modifiers, outputContent, name)));
    }

    private static String renderTrait(List<String> modifiers, String content, String name) {
        final var outputModifiers = modifiers.isEmpty() ? "" : String.join(" ", modifiers) + " ";
        return outputModifiers + "trait " + name + " {" + content + "\n}";
    }

    private static Option<Result<String, CompileError>> compileRecord(String rootSegment) {
        final var keywordIndex = rootSegment.indexOf("record ");
        if (keywordIndex == -1) return new None<>();

        final var modifiersString = rootSegment.substring(0, keywordIndex);
        final var newModifiers = parseClassModifiers(modifiersString);

        final var afterKeyword = rootSegment.substring(keywordIndex + "record ".length());
        final var paramStart = afterKeyword.indexOf('(');
        if (paramStart == -1) return new None<>();

        final var name = afterKeyword.substring(0, paramStart).strip();
        final var afterOpenParentheses = afterKeyword.substring(paramStart + 1).strip();
        final var paramEnd = afterOpenParentheses.indexOf(')');
        if (paramEnd == -1) return new None<>();

        final var params = afterOpenParentheses.substring(0, paramEnd).strip();
        final var paramsOut = parseParams(params);

        final var afterParams = afterOpenParentheses.substring(paramEnd + 1);
        final var contentStart = afterParams.indexOf('{');
        if (contentStart == -1) return new None<>();

        final var maybeImplements = afterParams.substring(0, contentStart).strip();
        final var impl = findImpl(maybeImplements);

        final var withEnd = afterParams.substring(contentStart + 1).strip();
        final var contentEnd = withEnd.lastIndexOf('}');
        if (contentEnd == -1) return new None<>();
        final var content = withEnd.substring(0, contentEnd);

        return new Some<>(parseAndCompile(content, Main::compileInnerMember)
                .flatMapValue(outputContent -> generateFunction(new Node()
                        .withStringList("modifiers", newModifiers)
                        .withString("name", name)
                        .withString("params", paramsOut)
                        .withString("content", outputContent + impl))));
    }

    private static String parseParams(String params) {
        return Arrays.stream(params.split(","))
                .map(String::strip)
                .filter(paramSegment -> !paramSegment.isEmpty())
                .map(Main::parseParameter)
                .flatMap(Options::stream)
                .collect(Collectors.joining(", "));
    }

    private static Option<String> parseParameter(String paramSegment) {
        final var separator = paramSegment.indexOf(' ');
        if (separator == -1) return new None<>();

        final var paramType = paramSegment.substring(0, separator).strip();
        final var paramName = paramSegment.substring(separator + 1).strip();
        return new Some<>(paramName + ": " + paramType);
    }

    private static String findImpl(String maybeImplements) {
        if (!maybeImplements.startsWith("implements ")) return "";

        final var traitName = maybeImplements.substring("implements ".length());
        return renderImpl(traitName);
    }

    private static Result<String, CompileError> compileInnerMember(String innerMember) {
        return compileMethod(innerMember)
                .orElseGet(() -> new Err<>(new CompileError("Unknown inner member", new StringContext(innerMember))));
    }

    private static Option<Result<String, CompileError>> compileMethod(String innerMember) {
        final var paramStart = innerMember.indexOf("(");
        if (paramStart == -1) return new None<>();

        final var header = innerMember.substring(0, paramStart).strip();
        final var nameSeparator = header.lastIndexOf(' ');

        final var beforeName = header.substring(0, nameSeparator).strip();
        final var typeSeparator = beforeName.lastIndexOf(' ');
        final String type = typeSeparator == -1
                ? beforeName
                : beforeName.substring(typeSeparator + 1).strip();

        final var name = header.substring(nameSeparator + 1).strip();

        final var afterHeader = innerMember.substring(paramStart + 1).strip();
        final var contentStart = afterHeader.indexOf('{');
        final var header0 = "\n\tdef " + name + "(this : &This): " + type;
        if (contentStart == -1) {
            return new Some<>(new Ok<>(header0 + ";"));
        }
        final var withEnd = afterHeader.substring(contentStart + 1).strip();

        final var contentEnd = withEnd.lastIndexOf('}');
        if (contentEnd == -1) return new None<>();
        final var content = withEnd.substring(0, contentEnd);
        return new Some<>(parseAndCompile(content, Main::compileStatement)
                .mapErr(err -> new CompileError("Invalid content", new StringContext(content), err))
                .mapValue(outputContent -> header0 + " => {" + outputContent + "\n\t}"));

    }

    private static Result<String, CompileError> compileStatement(String statement) {
        return compileReturn(statement)
                .or(() -> compileInvocation(statement))
                .or(() -> compileDeclaration(statement))
                .orElseGet(() -> new Err<>(new CompileError("Unknown statement", new StringContext(statement))));
    }

    private static Option<Result<String, CompileError>> compileDeclaration(String statement) {
        return statement.contains("=") ? new Some<>(new Ok<>("let temp = 0;")) : new None<>();
    }

    private static Option<Result<String, CompileError>> compileInvocation(String statement) {
        return statement.contains("(")
                ? new Some<>(new Ok<>("todo();"))
                : new None<>();
    }

    private static Option<Result<String, CompileError>> compileReturn(String statement) {
        return statement.startsWith("return ")
                ? new Some<>(new Ok<>("\n\t\t" + statement))
                : new None<>();
    }

    private static List<String> parseClassModifiers(String modifiersString) {
        final var newModifiers = new ArrayList<>(parseModifiers(modifiersString));
        newModifiers.add("class");
        return newModifiers;
    }

    private static ArrayList<String> parseModifiers(String modifiersString) {
        final var modifiersArray = modifiersString.strip().split(" ");
        final var oldModifiers = Arrays.stream(modifiersArray)
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .toList();

        var newModifiers = new ArrayList<String>();
        if (oldModifiers.contains("public")) {
            newModifiers.add("export");
        }
        return newModifiers;
    }

    private static Option<Result<String, CompileError>> compileClass(String rootSegment) {
        final var keywordIndex = rootSegment.indexOf("class");
        if (keywordIndex == -1) return new None<>();
        final var beforeKeyword = rootSegment.substring(0, keywordIndex).strip();
        final var modifiers = parseClassModifiersToNode(beforeKeyword);

        final var afterKeyword = rootSegment.substring(keywordIndex + "class".length()).strip();

        final var contentStart = afterKeyword.indexOf("{");
        if (contentStart == -1) return new None<>();
        final var beforeContent = afterKeyword.substring(0, contentStart).strip();
        final var header = parseHeader(beforeContent);

        final var afterContent = afterKeyword.substring(contentStart + "{".length()).strip();
        if (!afterContent.endsWith("}")) return new None<>();
        final var content = afterContent.substring(0, afterContent.length() - "}".length()).strip();

        return new Some<>(parseAndCompile(content, Main::compileInnerMember).flatMapValue(output -> {
            final var node = header.mapString("content", impl -> output + impl).orElse(header);
            final var merge = new Node().merge(node);
            return generateFunction(merge.merge(modifiers));
        }));
    }

    private static Node parseClassModifiersToNode(String modifierString) {
        final var outputModifiers = parseClassModifiers(modifierString);
        return new Node().withStringList("modifiers", outputModifiers);
    }

    private static Node parseHeader(String nameAndMaybeImpl) {
        final var implementsIndex = nameAndMaybeImpl.indexOf("implements");
        if (implementsIndex == -1) {
            return new Node().withString("name", nameAndMaybeImpl);
        }

        final String name = nameAndMaybeImpl.substring(0, implementsIndex).strip();
        final var slice = nameAndMaybeImpl.substring(implementsIndex + "implements".length()).strip();
        return new Node()
                .withString("name", name)
                .withString("content", renderImpl(slice));
    }

    private static String renderImpl(String slice) {
        return "\n\timpl " + slice + ";";
    }

    private static Result<String, CompileError> generateFunction(Node node) {
        final var joined = node.findStringList("modifiers")
                .map(values -> String.join(" ", values) + " ")
                .orElse("");

        final var nameOption = node.findString("name");
        if (nameOption.isEmpty()) return new Err<>(new CompileError("No name present", new NodeContext(node)));

        final var name = nameOption.orElse("");

        final var params = node.findString("params").orElse("");
        final var content = node.findString("content").orElse("");

        return new Ok<>(joined + " def " + name + "(" + params + ") => {" + content + "\n}");
    }

    private static Option<Result<String, CompileError>> compileImport(String rootSegment) {
        if (rootSegment.startsWith("import ")) return new Some<>(new Ok<>(rootSegment + "\n"));
        return new None<>();
    }

    private static Option<Result<String, CompileError>> compilePackage(String rootSegment) {
        return rootSegment.startsWith("package ") ? new Some<>(new Ok<>("")) : new None<>();
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }
}
