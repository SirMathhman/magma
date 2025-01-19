#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Err<T, X> {
	X error;
	X> flatMapValue(){
		temp = temp;
	}
	X> mapValue(){
		temp = temp;
	}
	R> mapErr(){
		temp = temp;
	}
	R match(){
		return onErr.apply(this.error);
	}
	X> and(){
		temp = temp;
	}
	R>> or(){
		return other.get().mapErr(otherErr -> new Tuple<>(this.error, otherErr));
	}
};