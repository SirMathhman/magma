package magma.app.compile.lang.casm;

import magma.app.compile.Node;
import magma.app.compile.lang.CASMLang;

public class DataFormatter implements Modifier {
    @Override
    public Node modify(Node node) {
        return node
                .withString(CASMLang.DATA_AFTER_NAME, " ")
                .withString(CASMLang.DATA_BEFORE_VALUE, " ");
    }
}