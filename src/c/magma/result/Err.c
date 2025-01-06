#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Err<T, X> {
	X error;
	impl Result<T, X> {
		Optional<T> findValue(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Optional<X> findError(void* __ref__){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<[T, R], X> and(void* __ref__, [Closure, (Closure) => Result<R, X>] other){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<R, X> mapValue(void* __ref__, Function<T, R> mapper){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<R, X> flatMapValue(void* __ref__, Function<T, Result<R, X>> mapper){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<T, R> mapErr(void* __ref__, Function<X, R> mapper){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
	}
};