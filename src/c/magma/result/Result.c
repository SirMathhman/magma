#include "temp.h"
struct Result<T, X> {
	Result<R, X> mapValue(Function<T, R> mapper);
	R match(Function<T, R> onOkFunction<X, R> onErr);
	Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper);
};