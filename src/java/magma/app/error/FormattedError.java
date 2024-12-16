package magma.app.error;

public interface FormattedError extends Error {
    String format(int depth);
}
