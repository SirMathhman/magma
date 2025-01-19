#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Ok<T, X> {
	T value;
	struct Ok<T, X> Ok<T, X>_new(T value){
		struct Ok<T, X> this;
		this.value = value;
		return this;
	}
	X> Ok<T, X>_flatMapValue(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		return mapper.apply(this.value);
	}
	X> Ok<T, X>_mapValue(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		temp = temp;
	}
	R> Ok<T, X>_mapErr(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		temp = temp;
	}
	R Ok<T, X>_match(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		return onOk.apply(this.value);
	}
	X> Ok<T, X>_and(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
	}
	R>> Ok<T, X>_or(void* _this_){
		struct Ok<T, X> this = *(struct Ok<T, X>*) this;
		temp = temp;
	}
};