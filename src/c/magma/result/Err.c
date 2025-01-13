#include "temp.h"
#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(X error){
		= error;
	}
	Result<R, X> mapValue((T => R) mapper){
		return temp();
	}
	R match((T => R) onOk, (X => R) onErr){
		return onErr.apply(this.error);
	}
	Result<R, X> flatMapValue((T => Result<R, X>) mapper){
		return temp();
	}
	Optional<T> findValue(){
		return Optional.empty();
	}
};