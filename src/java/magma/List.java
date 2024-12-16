package magma;

public interface List<T> {
    List<T> add(T other);

    Stream<T> stream();
}
