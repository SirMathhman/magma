package magma.app.rule.divide;

import magma.api.result.Result;
import magma.app.error.CompileError;

import java.util.List;

public interface Divider {
    String merge(String current, String value);

    Result<List<String>, CompileError> divide(String input);
}
