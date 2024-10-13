package magma.rule;

import magma.GenerateException;
import magma.Node;
import magma.ParseException;
import magma.result.Result;

public interface Rule {
    Result<Node, ParseException> parse(String input);

    Result<String, GenerateException> generate(Node node);
}
