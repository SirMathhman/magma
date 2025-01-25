package magma.app.compile.node;

public interface MergeStrategy {
    <T> T merge(T left, T right);
}
