package magma.option;

public interface Option<T> {
    boolean isPresent();

    T orElseNull();
}
