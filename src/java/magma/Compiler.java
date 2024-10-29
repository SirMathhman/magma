package magma;

import magma.core.String_;
import magma.java.JavaString;

public record Compiler(String_ input) {
    static JavaString renderImport() {
        return new JavaString("import magma;");
    }

    String_ compile() {
        if (input.equalsTo(renderImport())) {
            return input;
        } else {
            return JavaString.EMPTY;
        }
    }
}