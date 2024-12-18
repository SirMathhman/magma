package magma.app.compile.rule;

import magma.app.compile.Node;
import magma.api.result.Result;
import magma.app.error.FormattedError;

public interface Rule {
    Result<Node, FormattedError> parse(Input input);

    Result<String, FormattedError> generate(Node node);
}
