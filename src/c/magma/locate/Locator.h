#include <temp.h>
struct Locator {
	struct Locator new(){
		struct Locator this;
		return this;
	}
	String unwrap(void* _this_);
	int length(void* _this_);
	Optional<Integer> locate(void* _this_);
};