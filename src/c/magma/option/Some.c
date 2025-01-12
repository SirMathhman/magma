import magma.Tuple;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
struct Some<T>(T value) implements Option<T> {
	R match(Function<T, R> ifPresent, Supplier<R> ifEmpty){
		return ifPresent.apply(this.value);
	}
	void ifPresent(Consumer<T> consumer){
		consumer.accept(this.value);
	}
	Option<R> map(Function<T, R> mapper){
		return Some<>(mapper.apply(this.value));
	}
	T orElseGet(Supplier<T> other){
		return this.value;
	}
	T> toTuple(T other){
		return Tuple<>(true, this.value);
	}
	Option<T> or(Supplier<Option<T>> other){
		return this;
	}
	boolean isEmpty(){
		return false;
	}
	boolean isPresent(){
		return true;
	}
	T unwrap(){
		return this.value;
	}
	T orElse(T other){
		return this.value;
	}
	Option<R> flatMap(Function<T, Option<R>> mapper){
		return mapper.apply(this.value);
	}
	Option<T> filter(Predicate<T> predicate){
		return predicate.test(this.value) ? this : new None<>();
	}
}