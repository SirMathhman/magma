package magma.app;

public record ThrowableError(Throwable throwable) implements Error {
    @Override
    public String format(int depth, int index) {
        return throwable.toString();
    }
}
