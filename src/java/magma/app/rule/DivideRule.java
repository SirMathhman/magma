package magma.app.rule;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.Node;
import magma.app.error.CompileError;

import java.util.ArrayList;
import java.util.List;

public class DivideRule implements Rule {
    private final Divider divider;
    private final Rule childRule;

    public DivideRule(Divider divider, Rule childRule) {
        this.divider = divider;
        this.childRule = childRule;
    }

    public static Result<List<Node>, CompileError> compileAll(List<String> segments, Rule rule) {
        Result<List<Node>, CompileError> nodes = new Ok<>(new ArrayList<>());
        for (String segment : segments) {
            final var stripped = segment.strip();
            if (stripped.isEmpty()) continue;

            nodes = nodes.and(() -> rule.parse(stripped)).mapValue(tuple -> {
                tuple.left().add(tuple.right());
                return tuple.left();
            });
        }
        return nodes;
    }
}
