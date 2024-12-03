package magma.option;

public class None<T> implements Option<T> {
    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public T orElseNull() {
        return null;
    }
}
