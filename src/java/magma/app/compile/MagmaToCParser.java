package magma.app.compile;

import magma.app.compile.pass.PassingStage;

public class MagmaToCParser implements PassingStage {
    @Override
    public Node pass(Node node) {
        return node;
    }
}
