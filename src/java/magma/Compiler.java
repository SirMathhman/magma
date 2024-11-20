package magma;

import magma.result.Result;
import magma.rule.*;

import java.util.ArrayList;
import java.util.List;

public class Compiler {
    public static final String IMPORT_KEYWORD_WITH_SPACE = "import ";
    public static final String STATEMENT_END = ";";
    public static final String PACKAGE_KEYWORD_WITH_SPACE = "package ";
    public static final String STATIC_KEYWORD_WITH_SPACE = "static ";
    public static final String CLASS_KEYWORD_WITH_SPACE = "class ";
    public static final String BLOCK_EMPTY = " {}";
    public static final String CLASS_TYPE = "class";
    public static final String PACKAGE_TYPE = "package";
    public static final String VALUE = "value";
    public static final String IMPORT_TYPE = "import";
    public static final String IMPORT_STATIC_TYPE = "import-static";
    public static final String FUNCTION_TYPE = "function";

    static Result<String, CompileException> compile(String input) {
        final var sourceRule = new SplitRule("children", new OrRule(List.of(
                createPackageRule(),
                createStaticImportRule(),
                createInstanceImportRule(),
                createClassRule()
        )));

        final var targetRule = new SplitRule("children", new OrRule(List.of(
                createInstanceImportRule(),
                createFunctionRule()
        )));

        return sourceRule.parse(input)
                .mapValue(node -> node.findNodeList("children").orElse(new ArrayList<>()))
                .mapValue(Compiler::pass)
                .flatMapValue(nodes -> targetRule.generate(new Node().withNodeList("children", nodes)));
    }

    private static List<Node> pass(List<Node> sourceNodes) {
        return sourceNodes.stream()
                .filter(node -> !node.is(PACKAGE_TYPE))
                .map(Compiler::passRootMember)
                .toList();
    }

    private static Node passRootMember(Node node) {
        if (node.is(IMPORT_STATIC_TYPE)) return node.retype(IMPORT_TYPE);
        if (node.is(CLASS_TYPE)) return node.retype(FUNCTION_TYPE);
        return node;
    }

    public static Rule createStaticImportRule() {
        return createImportRule(IMPORT_STATIC_TYPE, new PrefixRule(STATIC_KEYWORD_WITH_SPACE, createNamespaceRule()));
    }

    public static Rule createInstanceImportRule() {
        return createImportRule(IMPORT_TYPE, createNamespaceRule());
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
        return new TypeRule(PACKAGE_TYPE, new PrefixRule(PACKAGE_KEYWORD_WITH_SPACE, createNamespaceRule()));
    }

    public static Rule createFunctionRule() {
        return new TypeRule(FUNCTION_TYPE, new PrefixRule("class def ", new SuffixRule(new StringRule(), "() => {}")));
    }
}