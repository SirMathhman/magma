#include "temp.h"
struct Ok<T, X> implements Result<T, X> {
	T value;
	public Ok();
	X> mapValue();
	R match();
	X> flatMapValue();
};