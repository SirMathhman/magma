package magma;

public interface Option<T> {
    boolean isPresent();

    T orElseNull();
}
