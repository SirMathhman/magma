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
        return createMagmaFunctionRule()
                .parse(input)
                .flatMap(Main::pass)
                .flatMap(node -> createCFunctionRule().generate(node))
                .orElse(input);
    }

    private static PrefixRule createMagmaFunctionRule() {
        return new PrefixRule(DEF_KEYWORD_WITH_SPACE, new InfixRule(new StringRule(NAME), PARAMS, new InfixRule(new StringRule("type"), CONTENT_START, new SuffixRule(new StringRule(CONTENT), AFTER_CONTENT))));
    }

    private static Optional<Node> pass(Node other) {
        final var node = other.mapString(CONTENT, s -> s.equals("return 0;") ? "\n\treturn 0;" : "").orElse(other);
        return node.mapString("type", type -> switch (type) {
            case "I32" -> "int";
            case "Void" -> "Void";
            default -> "";
        });
    }

    private static Rule createCFunctionRule() {
        final var type = new StringRule("type");
        final var name = new StringRule(NAME);
        final var beforeParams = new InfixRule(type, " ", name);
        final var content = new StringRule(CONTENT);
        return new SuffixRule(new InfixRule(beforeParams, "(){", content), "\n}");
    }
}
