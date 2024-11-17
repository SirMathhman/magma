package magma;

public class None<T> implements Option<T> {
    @Override
    public boolean isEmpty() {
        return true;
    }
}
