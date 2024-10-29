package magma;

import magma.core.String_;
import magma.java.JavaString;

public record Compiler(String_ input) {
    static String_ renderImport(JavaString namespace) {
        return namespace.prependSlice("import ").appendSlice(";");
    }

    String_ compile() {
        if (input.startsWithSlice("import ") && input.endsWithSlice(";")) {
            return input;
        } else {
            return JavaString.EMPTY;
        }
    }
}