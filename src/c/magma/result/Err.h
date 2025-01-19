#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Err<T, X> {
	X error;
	struct Err<T, X> Err<T, X>_new(X error){
		struct Err<T, X> this;
		this.error = error;
		return this;
	}
	X> Err<T, X>_flatMapValue(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	X> Err<T, X>_mapValue(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	R> Err<T, X>_mapErr(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	R Err<T, X>_match(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		return onErr.apply(this.error);
	}
	X> Err<T, X>_and(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	R>> Err<T, X>_or(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));
	}
};