#include "temp.h"
#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	Result<R, X> mapValue(Function<T, R> mapper){
		return temp();
	}
	R match(Function<T, R> onOk, Function<X, R> onErr){
		return onErr.apply(this.error);
	}
	Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){
		return temp();
	}
	Optional<T> findValue(){
		return Optional.empty();
	}
	boolean isOk(){
		return false;
	}
};