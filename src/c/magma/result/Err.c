#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Err<T, X> {
	X error;
	struct Err<T, X> new(X error){
		struct Err<T, X> this;
		this.error = error;
		return this;
	}
	X> flatMapValue(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	X> mapValue(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	R> mapErr(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	R match(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		return onErr.apply(this.error);
	}
	X> and(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		temp = temp;
	}
	R>> or(void* _this_){
		struct Err<T, X> this = *(struct Err<T, X>*) this;
		return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));
	}
};