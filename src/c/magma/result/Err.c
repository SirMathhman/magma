#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(X error){
	int value = 0;
	}
	Result<R, X> mapValue((T => R) mapper){
	new Err<>(this.error);
	}
	R match((T => R) onOk, (X => R) onErr){
	return onErr.apply(this.error);
	}
	Result<R, X> flatMapValue((T => Result<R, X>) mapper){
	new Err<>(this.error);
	}
};