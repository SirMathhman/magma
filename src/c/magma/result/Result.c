#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Result<T, X> {
	struct Result<T, X> new(){
		struct Result<T, X> this;
		return this;
	}
	X> flatMapValue(void* _this_);
	X> mapValue(void* _this_);
	R> mapErr(void* _this_);
	R match(void* _this_);
	X> and(void* _this_);
	R>> or(void* _this_);
};