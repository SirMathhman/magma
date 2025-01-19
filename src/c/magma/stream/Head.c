#include <temp.h>
struct Head<T> {
	Rc_Optional<T> next(void* _this_){
		struct Head<T> this = (struct Head<T>*) this;
	}
};