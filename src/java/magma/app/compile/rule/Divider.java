package magma.app.compile.rule;

import magma.api.collect.List;
import magma.api.result.Result;
import magma.app.error.FormattedError;

public interface Divider {
    StringBuilder concat(StringBuilder buffer, String slice);

    Result<List<Input>, FormattedError> divide(Input input);
}
