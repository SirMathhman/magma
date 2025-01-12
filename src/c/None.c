import magma.Tuple;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
struct None<T> implements Option<T> {public None() {
    }
	R match(Function<T, R> ifPresent, Supplier<R> ifEmpty){
		return ifEmpty.get();
	}
	void ifPresent(Consumer<T> consumer){
	}
	Option<R> map(Function<T, R> mapper){
		return None<>();
	}
	T orElseGet(Supplier<T> other){
		return other.get();
	}
	T> toTuple(T other){
		return Tuple<>(false, other);
	}
	Option<T> or(Supplier<Option<T>> other){
		return other.get();
	}
	boolean isEmpty(){
		return true;
	}
	boolean isPresent(){
		return false;
	}
	T unwrap(){
		throw new UnsupportedOperationException();
	}
	T orElse(T other){
		return other;
	}
	Option<R> flatMap(Function<T, Option<R>> mapper){
		return None<>();
	}
	Option<T> filter(Predicate<T> predicate){
		return None<>();
	}
}