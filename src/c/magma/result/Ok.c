#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	int value;
	void Ok(){
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