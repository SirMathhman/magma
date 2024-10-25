package magma.app.compile;

import magma.app.compile.lang.JavaLang;
import magma.app.compile.pass.ClassPasser;
import magma.app.compile.pass.ImportPasser;
import magma.app.compile.pass.InterfacePasser;
import magma.app.compile.pass.RecordPasser;

import java.util.List;

import static magma.app.compile.lang.CommonLang.CHILDREN;

public class Passer {
    static Node pass(Node node) {
        return node.mapNodeList(CHILDREN, Passer::passChildren).orElse(node);
    }

    private static List<Node> passChildren(List<Node> children) {
        return children.stream()
                .filter(child -> !child.is(JavaLang.PACKAGE))
                .map(Passer::passRootChild)
                .toList();
    }

    private static Node passRootChild(Node node) {
        return RecordPasser.passRecord(node)
                .or(() -> InterfacePasser.passInterface(node))
                .or(() -> ClassPasser.pass(node))
                .or(() -> ImportPasser.pass(node))
                .orElse(node);
    }
}
