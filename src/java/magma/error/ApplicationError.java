package magma.error;

public class ApplicationError implements Error {
    private final Error cause;

    public ApplicationError(Error cause) {
        this.cause = cause;
    }

    @Override
    public String display() {
        return this.cause.display();
    }
}
