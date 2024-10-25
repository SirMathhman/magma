package magma.app.compile.pass;

import magma.app.compile.Node;
import magma.app.compile.lang.MagmaLang;

import java.util.Optional;

import static magma.app.compile.lang.JavaLang.CLASS_TYPE;

public class ClassAdapter implements Passer {
    @Override
    public Optional<Node> beforePass(Node node) {
        if (!node.is(CLASS_TYPE)) return Optional.empty();

        return Optional.of(node.retype(MagmaLang.FUNCTION_TYPE));
    }
}
