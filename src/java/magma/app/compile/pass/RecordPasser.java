package magma.app.compile.pass;

import magma.app.compile.MapNode;
import magma.app.compile.Node;
import magma.app.compile.lang.CommonLang;
import magma.app.compile.lang.JavaLang;
import magma.app.compile.lang.MagmaLang;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static magma.app.compile.lang.CommonLang.*;

public class RecordPasser {
    public static Optional<Node> passRecord(Node node) {
        if (!node.is(JavaLang.RECORD_TYPE)) return Optional.empty();

        final var retyped = node.retype(MagmaLang.FUNCTION);

        final var withModifiers = retyped.mapNodeList(MODIFIERS, modifiers -> {
            final var copy = new ArrayList<Node>();
            if (hasPublicKeyword(modifiers)) {
                copy.add(createClassModifier("export"));
            }

            copy.add(createClassModifier("class"));
            return copy;
        }).orElseGet(() -> retyped.withNodeList(MODIFIERS, Collections.singletonList(createClassModifier("class"))));

        final var withImplements = withModifiers.mapNodeList(CommonLang.CHILDREN, children -> {
            var copy = new ArrayList<>(children);
            copy.add(new MapNode().retype("implements"));
            return copy;
        }).orElse(withModifiers);

        return Optional.of(withImplements);
    }

    private static boolean hasPublicKeyword(List<Node> modifiers) {
        return modifiers.stream()
                .map(modifier -> modifier.findString(MODIFIER_VALUE))
                .flatMap(Optional::stream)
                .anyMatch("public"::equals);
    }

    private static Node createClassModifier(String modifier) {
        return new MapNode().retype(MODIFIER_TYPE).withString(MODIFIER_VALUE, modifier);
    }
}