package magma.app;

public record ThrowableError(Throwable throwable) implements Error {
    @Override
    public String format(int depth) {
        return throwable.toString();
    }
}
