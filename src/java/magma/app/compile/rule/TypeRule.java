package magma.app.compile.rule;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.Input;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.error.CompileError;
import magma.app.error.context.NodeContext;
import magma.app.error.context.StringContext;
import magma.java.JavaOptions;

import java.util.List;

import static java.lang.System.out;

public record TypeRule(String type, Rule rule) implements Rule {
    @Override
    public Result<Node, CompileError> parse(String input) {
        return this.rule.parse(input)
                .mapValue(node -> node.retype(this.type))
                .mapValue(this::postProcess)
                .mapErr(err -> new CompileError("Failed to parse type '" + this.type + "'", new StringContext(input), List.of(err)));
    }

    private Node postProcess(Node node) {
        if (this.type.equals("method")) {
            Node node1 = JavaOptions.toNative(node.nodes().find("definition"))
                    .orElse(new MapNode());
            out.println("\t" + JavaOptions.toNative(node1.inputs().find("name").map(Input::unwrap))
                    .orElse(""));
        }

        return node;
    }

    @Override
    public Result<String, CompileError> generate(Node node) {
        if (node.is(this.type)) {
            return this.rule.generate(node)
                    .mapErr(err -> new CompileError("Failed to generate type '" + this.type + "'", new NodeContext(node), List.of(err)));
        } else {
            return new Err<>(new CompileError("Node was not of type '" + this.type + "'", new NodeContext(node)));
        }
    }
}
