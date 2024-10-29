package magma.compile;

import magma.compile.lang.CommonLang;
import magma.compile.rule.Rule;
import magma.core.String_;
import magma.core.option.Option;
import magma.core.result.Err;
import magma.core.result.Ok;
import magma.core.result.Result;

public record Compiler(String_ input) {
    private static Rule createRootRule() {
        return CommonLang.createImportRule();
    }

    public Result<String_, Error_> compile() {
        return compileImport()
                .<Result<String_, Error_>>map(Ok::new)
                .orElseGet(() -> new Err<>(ParseError.create("Invalid input", input)));
    }

    private Option<String_> compileImport() {
        return createRootRule().parse(input).flatMap(mapNode -> createRootRule().generate(mapNode));
    }
}