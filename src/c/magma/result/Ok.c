#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	T value;
	public Ok(T value){
	int value = 0;
	}
	Result<R, X> mapValue((T => R) mapper){
	new Ok<>(mapper.apply(this.value));
	}
	R match((T => R) onOk, (X => R) onErr){
	return onOk.apply(this.value);
	}
	Result<R, X> flatMapValue((T => Result<R, X>) mapper){
	return mapper.apply(this.value);
	}
};