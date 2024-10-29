package magma.compile.rule;

import magma.compile.CompileError;
import magma.compile.Node;
import magma.core.String_;
import magma.core.result.Err;
import magma.core.result.Result;

public record PrefixRule(String prefix, Rule childRule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String_ input) {
        return input.truncateLeftBySlice(this.prefix())
                .map(input1 -> this.childRule().parse(input1))
                .orElseGet(() -> {
                    final var format = "Prefix '%s' not present";
                    final var message = format.formatted(prefix);
                    final var error = CompileError.create(message, input);
                    return new Err<>(error);
                });
    }

    @Override
    public Result<String_, CompileError> generate(Node node) {
        return this.childRule().generate(node).mapValue(output -> output.prependSlice(this.prefix()));
    }
}