package magma.app.compile.pass;

import magma.app.compile.Node;

import java.util.Optional;

import static magma.app.compile.lang.CommonLang.IMPORT_TYPE;
import static magma.app.compile.lang.JavaLang.IMPORT_STATIC_TYPE;

public class ImportAdapter implements Passer {
    @Override
    public Optional<Node> beforePass(Node node) {
        if (!node.is(IMPORT_STATIC_TYPE)) return Optional.empty();

        return Optional.of(node.retype(IMPORT_TYPE));
    }
}
