import magma.api.result.Ok;
import magma.api.result.Result;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Override
Optional<T> foldLeft(BiFunction<T, T, T> folder){
	return this.head.next().map(initial -> foldLeft(initial, folder));
}

@Override
<R>R foldLeft(R initial, BiFunction<R, T, R> folder){
	auto current=initial;
	while(true){
		R finalCurrent=current;
		 auto maybeNext=this.head.next().map(next -> folder.apply(finalCurrent, next));
		if(maybeNext.isPresent()){
			current=maybeNext.get();
		}
		else {
			return current;
		}
	}
}

@Override
<R>Stream<R> map(Function<T, R> mapper){
	return new HeadedStream<>(auto _lambda33_(){
		return this.head.next().map(mapper);
	});
}

@Override
<R, X>Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder){
	return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
}
struct HeadedStream<T>(Head<T> head) implements Stream<T> {
}
