package magma.app.compile.pass.format;

import magma.app.compile.Node;
import magma.app.compile.pass.Passer;

import java.util.Optional;

import static magma.app.compile.lang.CommonLang.PADDING_IMPORT_AFTER;

public class ImportFormatter implements Passer {
    @Override
    public Optional<Node> afterPass(Node node) {
        return Optional.of(node.withString(PADDING_IMPORT_AFTER, "\n"));
    }
}
