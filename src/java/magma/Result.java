package magma;

public interface Result<T, X> {
    Option<T> findValue();

    Option<X> findError();
}
