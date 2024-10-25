package magma.app.compile.pass;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.*;

public class RecordPasser {
    public static Optional<Node> passRecord(Node node) {
        if (!node.is(JavaLang.RECORD_TYPE)) return Optional.empty();

        final var retyped = node.retype(MagmaLang.FUNCTION);

        final var withModifiers = retyped.mapNodeList(MODIFIERS, modifiers -> {
            final var copy = new ArrayList<>(modifiers);
            copy.add(createClassModifier());
            return copy;
        }).orElseGet(() -> retyped.withNodeList(MODIFIERS, Collections.singletonList(createClassModifier())));

        final var withImplements = withModifiers.mapNodeList(CommonLang.CHILDREN, children -> {
            var copy = new ArrayList<>(children);
            copy.add(new MapNode().retype("implements"));
            return copy;
        }).orElse(withModifiers);

        return Optional.of(withImplements);
    }

    private static Node createClassModifier() {
        return new MapNode().retype(MODIFIER_TYPE).withString(MODIFIER_VALUE, "class");
    }
}