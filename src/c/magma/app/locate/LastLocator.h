#include <temp.h>
struct LastLocator {
	String infix;
	struct LastLocator LastLocator_new(String infix){
		struct LastLocator this;
		this.infix = infix;
		return this;
	}
	Optional<Integer> LastLocator_locate(void* _this_){
		struct LastLocator this = *(struct LastLocator*) this;
		temp = temp;
		temp = temp;
	}
	String LastLocator_unwrap(void* _this_){
		struct LastLocator this = *(struct LastLocator*) this;
		return this.infix;
	}
	int LastLocator_length(void* _this_){
		struct LastLocator this = *(struct LastLocator*) this;
		return this.infix.length();
	}
};