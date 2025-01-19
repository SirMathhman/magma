#include <temp.h>
struct Head<T> {
	struct Head<T> new(){
		struct Head<T> this;
	}
	Optional<T> next();
};