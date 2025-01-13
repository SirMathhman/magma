#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	T value;
	public Ok(T value){
		to = from;
	}
	Result<R, X> mapValue(Function<T, R> mapper){
		return temp;
	}
	R match(Function<T, R> onOk, Function<X, R> onErr){
		return temp;
	}
	Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){
		return temp;
	}
};