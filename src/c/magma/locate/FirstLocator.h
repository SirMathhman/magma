#include <temp.h>
struct FirstLocator {
	String infix;
	struct FirstLocator new(String infix){
		struct FirstLocator this;
		this.infix = infix;
		return this;
	}
	String unwrap(void* _this_){
		struct FirstLocator this = *(struct FirstLocator*) this;
		return this.infix;
	}
	int length(void* _this_){
		struct FirstLocator this = *(struct FirstLocator*) this;
		return this.infix.length();
	}
	Optional<Integer> locate(void* _this_){
		struct FirstLocator this = *(struct FirstLocator*) this;
		temp = temp;
		temp = temp;
	}
};