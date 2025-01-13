#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err(X error){
		to = from;
	}
	X> mapValue(R> mapper){
		return temp;
	}
	R match(R> onOkR> onErr){
		return temp;
	}
	X> flatMapValue(X>> mapper){
		return temp;
	}
};