package magma.compile.rule;

import magma.compile.GenerateException;
import magma.compile.Node;
import magma.compile.ParseException;
import magma.result.Result;

public interface Rule {
    Result<Node, ParseException> parse(String input);

    Result<String, GenerateException> generate(Node node);
}
