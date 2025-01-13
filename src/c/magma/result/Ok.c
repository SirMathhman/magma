#include "temp.h"
#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	T value;
	Result<R, X> mapValue(Function<T, R> mapper){
		return temp();
	}
	R match(Function<T, R> onOk, Function<X, R> onErr){
		return onOk.apply(this.value);
	}
	Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){
		return mapper.apply(this.value);
	}
	Optional<T> findValue(){
		return Optional.of(this.value);
	}
	boolean isOk(){
		return true;
	}
};