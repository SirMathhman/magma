package magma.app.error;

public interface FormattedError extends Error {
    int computeMaxDepth();

    String format(int depth);
}
