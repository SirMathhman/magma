package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;

public class SuffixRule {
    public static Result<String, CompileError> truncateRight(String input, String slice) {
        if (input.endsWith(slice)) {
            return new Ok<String, CompileError>(input.substring(0, input.length() - slice.length()));
        } else {
            return new Err<String, CompileError>(new CompileError("Suffix '" + slice + "' not present", new StringContext(input)));
        }
    }
}