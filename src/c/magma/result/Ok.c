#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	T value;
	public Ok(T value){
	int value = 0;
	}
	Result<R, X> mapValue((T => R) mapper){
		return temp;
	}
	R match((T => R) onOk, (X => R) onErr){
		return temp;
	}
	Result<R, X> flatMapValue((T => Result<R, X>) mapper){
		return temp;
	}
};