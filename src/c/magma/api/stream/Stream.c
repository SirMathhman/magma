#include "../../../magma/api/result/Result.h"
#include "../../../java/util/Optional.h"
#include "../../../java/util/function/BiFunction.h"
#include "../../../java/util/function/Function.h"
struct Stream<T>{
	Optional<T> foldLeft(BiFunction<T, T, T> folder);
	<R>R foldLeft(R initial, BiFunction<R, T, R> folder);
	<R>Stream<R> map(Function<T, R> mapper);
	<R, X>Result<R, X> foldLeftToResult(R initial, BiFunction<R, T, Result<R, X>> folder);
}
