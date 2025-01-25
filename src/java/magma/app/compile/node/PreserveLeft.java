package magma.app.compile.node;

public class PreserveLeft implements MergeStrategy {
    @Override
    public <T> T merge(T left, T right) {
        return left;
    }
}
