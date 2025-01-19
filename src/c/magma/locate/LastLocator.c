#include <temp.h>
struct LastLocator {
	Rc_Optional<Integer> locate(void* _this_){
		struct LastLocator this = (struct LastLocator*) this;
		temp();
		temp();
	}
	Rc_String unwrap(void* _this_){
		struct LastLocator this = (struct LastLocator*) this;
		return value;
	}
	Rc_int length(void* _this_){
		struct LastLocator this = (struct LastLocator*) this;
		temp();
	}
};