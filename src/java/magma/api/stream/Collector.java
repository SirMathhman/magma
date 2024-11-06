package magma.api.stream;

public interface Collector<T, C> {
    C fold(C current, T element);

    C createInitial();
}
