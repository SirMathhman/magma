package magma.app.compile.lang.casm;

import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.lang.common.Generator;
import magma.app.compile.lang.magma.Stateless;
import magma.java.JavaList;

public class ExpandBlock implements Stateless {
    private final Generator generator;

    public ExpandBlock(Generator generator) {
        this.generator = generator;
    }

    @Override
    public Result<Node, CompileError> beforePass(Node node) {
        final var children = node.findNodeList("children").orElse(new JavaList<>());
        final var generatedName = generator.createUniqueName("block");
        return new Ok<>(CASMLang.label(generatedName, children.list()));
    }
}
