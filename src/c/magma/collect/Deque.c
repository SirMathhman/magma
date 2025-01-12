import magma.Tuple;
import magma.java.JavaOptionals;
import magma.option.Option;
struct Deque<T> {@Deprecated
    default T popOrPanic() {
        return JavaOptionals.from(pop()).map(Tuple::left).orElseThrow();
    }Deque<T> add(T next);boolean isEmpty();Option<Tuple<T, Deque<T>>> pop();Option<T> peek();
}