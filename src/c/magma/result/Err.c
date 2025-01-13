#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	int error;
	void Err(){
		to = from;
	}
	void mapValue(){
		return temp;
	}
	void match(){
		return temp;
	}
	void flatMapValue(){
		return temp;
	}
};