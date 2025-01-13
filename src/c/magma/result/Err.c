#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(X error){
		to = from;
	}
	Result<RX> mapValue(Function<TR> mapper){
		return temp;
	}
	R match(Function<TR> onOkFunction<XR> onErr){
		return temp;
	}
	Result<RX> flatMapValue(Function<TResult<RX>> mapper){
		return temp;
	}
};