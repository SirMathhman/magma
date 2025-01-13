#include "temp.h"
struct Result<T, X> {
	Result<RX> mapValue(Function<TR> mapper);
	R match(Function<TR> onOkFunction<XR> onErr);
	Result<RX> flatMapValue(Function<TResult<RX>> mapper);
};