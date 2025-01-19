package magma.app.rule;

import magma.api.result.Err;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.error.CompileError;
import magma.app.error.context.StringContext;

public class PrefixRule {
    public static Result<String, CompileError> truncateLeft(String input, String slice) {
        if (input.startsWith(slice)) return new Ok<String, CompileError>(input.substring(slice.length()));
        return new Err<String, CompileError>(new CompileError("Prefix '" + slice + "' not present", new StringContext(input)));
    }
}