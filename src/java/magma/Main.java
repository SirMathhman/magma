package magma;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static final String DEF_KEYWORD_WITH_SPACE = "def ";
    public static final String PARAMS = "(): ";
    public static final String CONTENT_START = " => {";
    public static final String AFTER_CONTENT = "}";
    public static final String CONTENT = "content";
    public static final String NAME = "name";

    private static String generate(String content) {
        return CONTENT_START +
                content +
                AFTER_CONTENT;
    }

    public static void main(String[] args) {
        try {
            final var source = Paths.get(".", "src", "magma", "main.mgs");
            final var input = Files.readString(source);
            Files.writeString(source.resolveSibling("main.c"), compile(input));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compile(String input) {
        return compileFunction(input).orElse(input);
    }

    private static Optional<String> compileFunction(String input) {
        if (!input.startsWith(DEF_KEYWORD_WITH_SPACE)) return Optional.empty();
        final var withoutKeyword = input.substring(DEF_KEYWORD_WITH_SPACE.length());

        final var paramsIndex = withoutKeyword.indexOf(PARAMS);
        if (paramsIndex == -1) return Optional.empty();
        final var beforeParams = withoutKeyword.substring(0, paramsIndex);
        final var afterParams = withoutKeyword.substring(paramsIndex + PARAMS.length());

        final var contentIndex = afterParams.indexOf(CONTENT_START);
        if (contentIndex == -1) return Optional.empty();
        final var beforeContent = afterParams.substring(0, contentIndex);
        final var afterContent = afterParams.substring(contentIndex + CONTENT_START.length()).strip();

        if (!afterContent.endsWith(AFTER_CONTENT)) return Optional.empty();
        var inputContent = afterContent.substring(0, afterContent.length() - AFTER_CONTENT.length()).strip();

        var outputContent = inputContent.equals("return 0;") ? "\n\treturn 0;" : "";
        final var type = switch (beforeContent) {
            case "I32" -> "int";
            case "Void" -> "Void";
            default -> "";
        };

        return createCFunctionRule().generate(new Node()
                .withString("type", type)
                .withString(NAME, beforeParams)
                .withString(CONTENT, outputContent));
    }

    private static Rule createCFunctionRule() {
        final var type = new StringRule("type");
        final var name = new StringRule(NAME);
        final var beforeParams = new InfixRule(type, " ", name);
        final var content = new StringRule(CONTENT);
        return new SuffixRule(new InfixRule(beforeParams, "(){", content), "\n}");
    }
}
