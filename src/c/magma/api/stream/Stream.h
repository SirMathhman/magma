#include "../../../magma/api/option/Option.h"
#include "../../../magma/api/result/Result.h"
#include "../../../java/util/function/BiFunction.h"
#include "../../../java/util/function/Function.h"
#include "../../../java/util/function/Predicate.h"
struct Stream<T> extends Head<T>{
	Option<T> foldLeft(BiFunction<T, T, T> folder);
	<R>R foldLeft(R initial, BiFunction<R, T, R> folder);
	<R>Stream<R> map(Function<T, R> mapper);
	<R, X>Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder);
	<R>Stream<R> flatMap(Function<T, Stream<R>> mapper);
	<C>C collect(Collector<T, C> collector);
	Stream<T> filter(Predicate<T> predicate);
	Stream<T> concat(Stream<T> other);
}
