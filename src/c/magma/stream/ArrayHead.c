import magma.option.None;
import magma.option.Option;
import magma.option.Some;
struct ArrayHead<T> implements Head<T> {private final T[] array;
	int counter = 0;public ArrayHead(T[] array) {
        this.array = array;
    }@Override
    public Option<T> next() {
        if (this.counter >= this.array.length) return new None<>();

        final var element = this.array[this.counter];
        this.counter++;
        return new Some<>(element);
    }
}