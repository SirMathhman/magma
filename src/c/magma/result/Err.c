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
		Result<Tuple<TR>X> and(void* __ref__, Supplier<Result<R, X>> other){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<RX> mapValue(void* __ref__, Function<T, R> mapper){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<RX> flatMapValue(void* __ref__, Function<T, Result<R, X>> mapper){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
		Result<TR> mapErr(void* __ref__, Function<X, R> mapper){
			struct Err<T, X> this = *(struct Err<T, X>*) __ref__;
			return temp();
		}
	}
};