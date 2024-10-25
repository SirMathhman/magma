package magma.app.compile.pass;

import magma.app.compile.Node;
import magma.app.compile.lang.MagmaLang;

import java.util.Optional;

import static magma.app.compile.lang.JavaLang.INTERFACE_TYPE;

public class InterfaceAdapter implements Passer {
    @Override
    public Optional<Node> beforePass(Node node) {
        if (!node.is(INTERFACE_TYPE)) return Optional.empty();

        return Optional.of(node.retype(MagmaLang.TRAIT_TYPE));
    }
}
