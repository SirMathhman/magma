#include "temp.h"
struct Err<T, X> implements Result<T, X> {
	X error;
	public Err();
	X> mapValue();
	R match();
	X> flatMapValue();
};