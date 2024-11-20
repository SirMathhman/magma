package magma.rule;

import magma.CompileException;
import magma.Compiler;
import magma.Node;
import magma.result.Err;
import magma.result.Ok;
import magma.result.Result;

import java.util.Optional;

public class StringRule implements Rule {
    private Optional<Node> parse1(String input) {
        return Optional.of(new Node().withString(Compiler.VALUE, input));
    }

    private Optional<String> generate1(Node node) {
        return Optional.of(node.findValue(Compiler.VALUE).orElseThrow());
    }

    @Override
    public Result<Node, CompileException> parse(String input) {
        return parse1(input)
                .<Result<Node, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException()));
    }

    @Override
    public Result<String, CompileException> generate(Node node) {
        return generate1(node)
                .<Result<String, CompileException>>map(Ok::new)
                .orElseGet(() -> new Err<>(new CompileException()));
    }
}