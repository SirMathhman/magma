package magma;

import magma.error.CompileError;
import magma.result.Result;

public record SuffixRule(Rule childRule, String suffix) implements Rule {
    @Override
    public Result<String, CompileError> generate(String value) {
        return childRule().generate(value).mapValue(generated -> generated + suffix());
    }
}