package magma;

import magma.core.String_;
import magma.core.option.Option;
import magma.java.JavaString;

public record Compiler(String_ input) {

    public static final String IMPORT_PREFIX = "import ";
    public static final String STATEMENT_END = ";";

    static String_ renderImport(String_ namespace) {
        return namespace.prependSlice(IMPORT_PREFIX).appendSlice(STATEMENT_END);
    }

    String_ compile() {
        return compileImport().orElse(JavaString.EMPTY);
    }

    private Option<String_> compileImport() {
        return input.truncateLeftBySlice(IMPORT_PREFIX).flatMap(withoutLeft -> withoutLeft.truncateRightBySlice(STATEMENT_END).map(Compiler::renderImport));
    }
}