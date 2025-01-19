#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Ok<T, X> {
	T value;
	struct Ok<T, X> new(T value);
	X> flatMapValue(){
		return mapper.apply(this.value);
	}
	X> mapValue(){
		temp = temp;
	}
	R> mapErr(){
		temp = temp;
	}
	R match(){
		return onOk.apply(this.value);
	}
	X> and(){
		return other.get().mapValue(otherValue -> new Tuple<>(this.value, otherValue));
	}
	R>> or(){
		temp = temp;
	}
};