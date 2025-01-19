#include <temp.h>
struct Locator {
	struct Locator Locator_new(){
		struct Locator this;
		return this;
	}
	String Locator_unwrap(void* _this_);
	int Locator_length(void* _this_);
	Optional<Integer> Locator_locate(void* _this_);
};