import magma.api.result.Ok;
import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
public struct HeadedStream<T>(Head<T> head) implements Stream<T> {@Override
public Optional<T> foldLeft(BiFunction<T, T, T> folder){
	return this.head.next().map(initial -> foldLeft(initial, folder));
}@Override
public <R>R foldLeft(R initial, BiFunction<R, T, R> folder){
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
}@Override
public <R>Stream<R> map(Function<T, R> mapper){
	return new HeadedStream<>(() ->this.head.next().map(mapper));
}@Override
public <R, X>Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder){
	return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
}}
