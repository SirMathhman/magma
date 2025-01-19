#include <temp.h>
#include <temp.h>
#include <temp.h>
struct Stream<T> {
	struct Stream<T> new(){
		struct Stream<T> this;
		return this;
	}
	Optional<R> foldLeft();
	Stream<R> map();
};