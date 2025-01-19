#include <temp.h>
struct LastLocator {
	String infix;
	struct LastLocator new(String infix){
		struct LastLocator this;
		this.infix = infix;
		return this;
	}
	Optional<Integer> locate(void* _this_){
		struct LastLocator this = *(struct LastLocator*) this;
		temp = temp;
		temp = temp;
	}
	String unwrap(void* _this_){
		struct LastLocator this = *(struct LastLocator*) this;
		return this.infix;
	}
	int length(void* _this_){
		struct LastLocator this = *(struct LastLocator*) this;
		return this.infix.length();
	}
};