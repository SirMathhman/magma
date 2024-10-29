package magma;

import magma.core.String_;
import magma.core.option.Option;
import magma.java.JavaString;

public record Compiler(String_ input) {

    public static final String IMPORT_PREFIX = "import ";
    public static final String STATEMENT_END = ";";

    static String_ generateImport(Node mapNode) {
        return mapNode.find("namespace")
                .orElse(JavaString.EMPTY)
                .prependSlice(IMPORT_PREFIX)
                .appendSlice(STATEMENT_END);
    }

    String_ compile() {
        return compileImport().orElse(JavaString.EMPTY);
    }

    private Option<String_> compileImport() {
        return parseImport().map(Compiler::generateImport);
    }

    private Option<Node> parseImport() {
        return input.truncateLeftBySlice(IMPORT_PREFIX)
                .flatMap(withoutLeft -> withoutLeft.truncateRightBySlice(STATEMENT_END)
                        .flatMap(namespace -> {
                            final var node = new MapNode();
                            final var with = node.withString("namespace", namespace);
                            return with;
                        }));
    }
}