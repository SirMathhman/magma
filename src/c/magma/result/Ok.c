#include "temp.h"
#include "temp.h"
#include "temp.h"
struct Ok<T, X> {
	T value;
	impl Result<T, X> {
		Optional<T> findValue(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>*) __ref__;
			return temp();
		}
		Optional<X> findError(void* __ref__){
			struct Ok<T, X> this = *(struct Ok<T, X>*) __ref__;
			return temp();
		}
		Result<Tuple<T, R>, X> and(void* __ref__, Supplier<Result<R, X>> other){
			struct Ok<T, X> this = *(struct Ok<T, X>*) __ref__;
			return temp();
		}
		Result<R, X> mapValue(void* __ref__, Function<T, R> mapper){
			struct Ok<T, X> this = *(struct Ok<T, X>*) __ref__;
			return temp();
		}
		Result<R, X> flatMapValue(void* __ref__, Function<T, Result<R, X>> mapper){
			struct Ok<T, X> this = *(struct Ok<T, X>*) __ref__;
			return temp();
		}
	}
};