import magma.option.None;
import magma.option.Option;
struct EmptyHead<T> implements Head<T> {@Override
    public Option<T> next() {
        return new None<>();
    }
}