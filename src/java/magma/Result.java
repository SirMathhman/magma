package magma;

public interface Result<T, X> {
    boolean isErr();

    Option<X> findErr();

    Option<T> findValue();
}
