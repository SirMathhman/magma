package magma.app.compile.pass;

import magma.app.compile.Node;
import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;

import java.util.Optional;

public class InterfacePasser {
    public static Optional<Node> passInterface(Node node) {
        if (!node.is(JavaLang.INTERFACE_TYPE)) return Optional.empty();

        final var retype = node.retype(MagmaLang.TRAIT);

        final var withChildren = retype.mapNodeList(CommonLang.CHILDREN, children -> children.stream()
                        .map(ClassMemberPasser::passClassMember)
                        .toList())
                .orElse(retype);

        return Optional.of(withChildren);
    }
}