#include "../../../magma/api/option/Option.h"
#include "../../../magma/api/result/Ok.h"
#include "../../../magma/api/result/Result.h"
#include "../../../java/util/function/BiFunction.h"
#include "../../../java/util/function/Predicate.h"
struct HeadedStream<T>(Head<T> head) implements Stream<T>{
	Option<T> foldLeft(BiFunction<T, T, T> folder){
		return this.head.next().map(()->foldLeft(initial, folder));
	}
	<R>R foldLeft(R initial, BiFunction<R, T, R> folder){
		var current=initial;
		while(true){
			R finalCurrent=current;
			var maybeNext=this.head.next().map(()->folder.apply(finalCurrent, next)).toTuple(current);
			if(maybeNext.left()){
				current=maybeNext.right();
			}
			else{
				return current;
			}
		}
	}
	<R>Stream<R> map(Function<T, R> mapper){
		return new HeadedStream<>(()->this.head.next().map(mapper));
	}
	<R, X>Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder){
		return this.<Result<R, X>>foldLeft(new Ok<>(initial), (rxResult, t) -> rxResult.flatMapValue(inner -> folder.apply(inner, t)));
	}
	<R>Stream<R> flatMap(Function<T, Stream<R>> mapper){
		return map(mapper).foldLeft(Stream::concat).orElse(new HeadedStream<>(new EmptyHead<>()));
	}
	<C>C collect(Collector<T, C> collector){
		return foldLeft(collector.createInitial(), collector::fold);
	}
	Stream<T> filter(Predicate<T> predicate){
		return flatMap(()->new HeadedStream<>(predicate.test(value)
                ? new SingleHead<>(value)
                : new EmptyHead<>()));
	}
	Stream<T> concat(Stream<T> other){
		return new HeadedStream<>(()->this.head.next().or(other::next));
	}
	Option<T> next(){
		return this.head.next();
	}
}
