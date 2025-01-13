#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	T value;
	public Ok(T value){
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