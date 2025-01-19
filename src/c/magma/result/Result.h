#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Result<T, X> {
	struct Result<T, X> new(){
		struct Result<T, X> this;
		return this;
	}
	X> flatMapValue();
	X> mapValue();
	R> mapErr();
	R match();
	X> and();
	R>> or();
};