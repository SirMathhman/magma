package magma;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Compiler {
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATIC_KEYWORD_WITH_SPACE = "static ";
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";
    public static final String BLOCK_EMPTY = " {}";
    public static final String CLASS_TYPE = "class";
    public static final String PACKAGE_RULE = "package";

    static String compile(String input) throws CompileException {
        final var sourceRule = new OrRule(List.of(
                createPackageRule(),
                createInstanceImportRule(),
                createStaticImportRule(),
                createClassRule()
        ));

        final var targetRule = new OrRule(List.of(
                createInstanceImportRule(),
                createFunctionRule()
        ));

        final var segments = split(input);
        final var sourceNodes = segments.stream()
                .map(sourceRule::parse)
                .flatMap(Optional::stream)
                .toList();

        final var targetNodes = sourceNodes.stream()
                .filter(node -> !node.is(PACKAGE_RULE))
                .toList();

        return targetNodes.stream()
                .map(targetRule::generate)
                .flatMap(Optional::stream)
                .collect(Collectors.joining());
    }

    public static Rule createStaticImportRule() {
        return createImportRule("import-static", new PrefixRule(STATIC_KEYWORD_WITH_SPACE, createNamespaceRule()));
    }

    public static Rule createInstanceImportRule() {
        return createImportRule("import", createNamespaceRule());
    }

    public static Rule createClassRule() {
        return new TypeRule(CLASS_TYPE, new PrefixRule(CLASS_KEYWORD_WITH_SPACE, new SuffixRule(new StringRule(), BLOCK_EMPTY)));
    }

    private static SuffixRule createNamespaceRule() {
        return new SuffixRule(new StringRule(), STATEMENT_END);
    }

    private static Rule createImportRule(String type, Rule suffixRule) {
        return new TypeRule(type, new PrefixRule(IMPORT_KEYWORD_WITH_SPACE, suffixRule));
    }

    static ArrayList<String> split(String input) {
        var segments = new ArrayList<String>();
        var buffer = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            var c = input.charAt(i);
            buffer.append(c);
            if (c == ';') {
                advance(buffer, segments);
                buffer = new StringBuilder();
            }
        }
        advance(buffer, segments);
        return segments;
    }

    private static void advance(StringBuilder buffer, ArrayList<String> segments) {
        if (!buffer.isEmpty()) segments.add(buffer.toString());
    }

    public static Rule createPackageRule() {
        return new TypeRule(PACKAGE_RULE, new PrefixRule(PACKAGE_KEYWORD_WITH_SPACE, createNamespaceRule()));
    }

    public static PrefixRule createFunctionRule() {
        return new PrefixRule("class def ", new SuffixRule(new StringRule(), "() => {}"));
    }
}