import magma.api.result.Ok;import magma.api.result.Result;import java.util.Optional;struct HeadedStream<T>(Head<T> head) implements Stream<T>{
	Optional<T> foldLeft([void*, T (*)(void*, T, T)] folder){
		return this.head.next().map(()->foldLeft(initial, folder));
	}
	<R>R foldLeft(R initial, [void*, R (*)(void*, R, T)] folder){
		var current=initial;
		while(true){
			R finalCurrent=current;
			var maybeNext=this.head.next().map(()->folder.apply(finalCurrent, next));
			if(maybeNext.isPresent()){
				current=maybeNext.get();
			}
			else{
				return current;
			}
		}
	}
	<R>Stream<R> map([void*, R (*)(void*, T)] mapper){
		return HeadedStream<>.new();
	}
	<R, X>Result<R, X> foldLeftToResult(R initial, [void*, Result<R, X> (*)(void*, R, T)] folder){
		return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
	}
	struct HeadedStream new(){
		struct HeadedStream this;
		return this;
	}
}