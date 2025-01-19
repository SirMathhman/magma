#include <temp.h>
struct Head<T> {
	struct Head<T> new(){
		struct Head<T> this;
		return this;
	}
	Optional<T> next();
};