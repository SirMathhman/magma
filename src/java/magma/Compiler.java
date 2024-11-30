package magma;

import magma.api.result.Err;
import magma.api.result.Result;
import magma.app.compile.CompileError;
import magma.app.compile.MagmaLang;
import magma.app.compile.Node;
import magma.app.error.NodeContext;
import magma.java.JavaList;

import java.util.ArrayList;
import java.util.List;

import static magma.app.assemble.Operator.JumpByValue;
import static magma.app.compile.CASMLang.*;

public class Compiler {
    public static final String ROOT_CHILDREN = "children";
    public static final String STACK_POINTER = "__stack-pointer__";
    public static final String SPILL = "__spill__";

    static Result<Node, CompileError> compile(String source) {
        return MagmaLang.createMagmaRootRule()
                .parse(source)
                .flatMapValue(Compiler::compileRoot)
                .mapValue(Compiler::mergeIntoRoot);
    }

    private static Node mergeIntoRoot(List<Node> compiled) {
        var count = 0;
        for (Node node : compiled) {
            if (node.is(LABEL_TYPE)) {
                count += node.findNodeList(LABEL_CHILDREN).map(JavaList::size).orElse(0);
            } else {
                count += 1;
            }
        }

        final var instruct = new ArrayList<>(List.of(
                instruct(JumpByValue, "__start__"),
                data(STACK_POINTER, count + 6),
                data(SPILL, 0)
        ));

        instruct.addAll(compiled);
        return new Node(Main.ROOT_TYPE).withNodeList0(ROOT_CHILDREN, new JavaList<>(instruct));
    }

    private static Result<List<Node>, CompileError> compileRoot(Node root) {
        return new Err<>(new CompileError("Unknown root", new NodeContext(root)));
    }
}