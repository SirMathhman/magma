package magma.compile;

import magma.compile.lang.CommonLang;
import magma.compile.rule.Rule;
import magma.core.String_;
import magma.core.option.Option;
import magma.java.JavaString;

public record Compiler(String_ input) {
    private static Rule createRootRule() {
        return CommonLang.createImportRule();
    }

    public String_ compile() {
        return compileImport().orElse(JavaString.EMPTY);
    }

    private Option<String_> compileImport() {
        return createRootRule().parse(input).flatMap(mapNode -> createRootRule().generate(mapNode));
    }
}