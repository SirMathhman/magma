#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(){
		to = from;
	}
	X> mapValue(){
		return temp;
	}
	R match(){
		return temp;
	}
	X> flatMapValue(){
		return temp;
	}
};