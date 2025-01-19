#include <temp.h>
struct Locator {
	Rc_String unwrap(void* _this_){
		struct Locator this = (struct Locator*) this;
	}
	Rc_int length(void* _this_){
		struct Locator this = (struct Locator*) this;
	}
	Rc_Optional<Integer> locate(void* _this_){
		struct Locator this = (struct Locator*) this;
	}
};