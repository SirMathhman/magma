#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Stream<T> {
	struct Stream<T> new(){
		struct Stream<T> this;
		return this;
	}
	Optional<R> foldLeft(void* _this_);
	Stream<R> map(void* _this_);
};