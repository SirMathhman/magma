package magma.app.compile.pass;

import magma.api.list.List;
import magma.app.compile.Node;

public record SequentialPassingStage(List<PassingStage> stages) implements PassingStage {
    @Override
    public Node pass(Node node) {
        return stages.stream().foldRight(node, (current, stage) -> stage.pass(current));
    }
}
