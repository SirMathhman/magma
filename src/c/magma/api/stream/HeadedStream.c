import magma.api.result.Ok;
import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
public struct HeadedStream<T>(Head<T> head) implements Stream<T> {
	Optional<T> foldLeft=Optional<T> foldLeft(((T, T) => T) folder){
		return this.head.next().map(initial -> foldLeft(initial, folder));
	};
	<R>R foldLeft=<R>R foldLeft(R initial, ((R, T) => R) folder){
		var current=initial;
		while(true){
		R finalCurrent=current;
		final var maybeNext=this.head.next().map(next -> folder.apply(finalCurrent, next));
		if(maybeNext.isPresent()){
		current=maybeNext.get();
	}
		else {
		return current;
	}
	}
	};
	<R>Stream<R> map=<R>Stream<R> map(((T) => R) mapper){
		return new HeadedStream<>(() ->this.head.next().map(mapper));
	};
	<R, X>Result<R, X> foldLeftToResult=<R, X>Result<R, X> foldLeftToResult(R initial, ((R, T) => Result<R, X>) folder){
		return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
	};
}