#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(X error){
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