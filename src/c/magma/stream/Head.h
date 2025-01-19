#include <temp.h>
struct Head<T> {
	struct Head<T> new();
	Optional<T> next();
};