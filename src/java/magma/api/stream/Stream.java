package magma.api.stream;

public interface Stream<T> {
    <C> C collect(Collector<T, C> collector);
}
