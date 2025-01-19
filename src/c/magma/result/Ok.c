#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Ok<T, X> {
	T value;
	struct Ok<T, X> new(T value){
		struct Ok<T, X> this;
		this.value = value;
		return this;
	}
	X> flatMapValue(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		return mapper.apply(this.value);
	}
	X> mapValue(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		temp = temp;
	}
	R> mapErr(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		temp = temp;
	}
	R match(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		return onOk.apply(this.value);
	}
	X> and(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
	}
	R>> or(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		temp = temp;
	}
};