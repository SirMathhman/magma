#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(X error){
		this.error  = from;
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