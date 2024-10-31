package magma.compile.rule;

import magma.compile.CompileError;
import magma.compile.Node;
import magma.core.String_;
import magma.core.result.Err;
import magma.core.result.Result;
import magma.java.JavaList;

public record OrRule(JavaList<Rule> childRules) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String_ input) {
        return childRules.stream()
                .map(rule -> rule.parse(input))
                .filter(Result::isOk)
                .next()
                .orElseGet(() -> new Err<>(CompileError.create("No valid parsing rule", input)));
    }

    @Override
    public Result<String_, CompileError> generate(Node node) {
        return childRules.stream()
                .map(rule -> rule.generate(node))
                .filter(Result::isOk)
                .next()
                .orElseGet(() -> new Err<>(CompileError.create("No valid generating rule", node)));
    }
}
