package magma.app.compile.lang.magma;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.result.Ok;
import magma.api.result.Result;
import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.error.CompileError;
import magma.app.compile.pass.Passer;
import magma.java.JavaList;

import static magma.app.compile.lang.magma.MagmaLang.*;

public class FlattenBlock implements Passer {
    @Override
    public Option<Result<Node, CompileError>> afterNode(Node node) {
        if (!node.is(BLOCK_TYPE)) return new None<>();

        return node.mapNodeList(BLOCK_CHILDREN, FlattenBlock::flattenChildren);
    }

    private static Result<JavaList<Node>, CompileError> flattenChildren(JavaList<Node> children) {
        return new Ok<>(children.stream()
                .map(FlattenBlock::flattenChild)
                .flatMap(JavaList::stream)
                .foldLeft(new JavaList<Node>(), JavaList::add));
    }

    private static JavaList<Node> flattenChild(Node child) {
        if (child.is(DECLARATION_TYPE)) {
            final var name = child.findString(DECLARATION_NAME).orElse("");
            final var type = child.findNode(DECLARATION_TYPE_PROPERTY).orElse(new MapNode());
            final var value = child.findNode(DECLARATION_VALUE).orElse(new MapNode());

            final var definition = new MapNode("definition")
                    .withString("name", name)
                    .withNode("type", type);

            final var symbol = new MapNode("symbol")
                    .withString("value", name);

            final var assignment = new MapNode("assignment")
                    .withNode("variable", symbol)
                    .withNode("expression", value);

            return new JavaList<Node>()
                    .add(definition)
                    .add(assignment);
        } else {
            return new JavaList<Node>().add(child);
        }
    }
}
