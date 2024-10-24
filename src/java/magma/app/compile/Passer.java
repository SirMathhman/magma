package magma.app.compile;

import magma.app.compile.lang.JavaLang;
import magma.app.compile.pass.ClassPasser;
import magma.app.compile.pass.ImportPasser;
import magma.app.compile.pass.InterfacePasser;
import magma.app.compile.pass.RecordPasser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static magma.app.compile.lang.CommonLang.*;
import static magma.app.compile.lang.JavaLang.*;

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

    public static Optional<Node> passRootMemberModifiers(Node node) {
        if (node.is(CLASS_TYPE) || node.is(INTERFACE_TYPE) || node.is(RECORD_TYPE)) return Optional.empty();

        return node.mapNodeList(MODIFIERS, modifiers -> {
            final var inputModifiers = modifiers.stream()
                    .map(modifier -> modifier.findString(MODIFIER_VALUE))
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());

            var newList = new ArrayList<String>();
            if (inputModifiers.contains("public")) newList.add("export");

            return newList.stream()
                    .map(modifier -> new MapNode().retype(MODIFIER_TYPE).withString(MODIFIER_VALUE, modifier))
                    .toList();
        });
    }

}
