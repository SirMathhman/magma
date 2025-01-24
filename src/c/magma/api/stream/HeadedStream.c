import magma.api.result.Ok;import magma.api.result.Result;import java.util.Optional;struct HeadedStream<T>(Head<T> head){
	Optional<T> foldLeft(any* _ref_, Tuple<any*, T (*)(any*, T, T)> folder){
		return this.head.next().map(()->foldLeft(initial, folder));
	}
	<R>R foldLeft(any* _ref_, R initial, Tuple<any*, R (*)(any*, R, T)> folder){
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
	<R>Stream<R> map(any* _ref_, Tuple<any*, R (*)(any*, T)> mapper){
		return new HeadedStream<>(()->this.head.next().map(mapper));
	}
	<R, X>Result<R, X> foldLeftToResult(any* _ref_, R initial, Tuple<any*, Result<R, X> (*)(any*, R, T)> folder){
		return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
	}
	Stream<T> Stream(any* _ref_){
		return Stream.new();
	}
}