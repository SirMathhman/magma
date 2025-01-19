#include <temp.h>
struct FirstLocator {
	String infix;
	struct FirstLocator FirstLocator_new(String infix){
		struct FirstLocator this;
		this.infix = infix;
		return this;
	}
	String FirstLocator_unwrap(void* _this_){
		struct FirstLocator this = *(struct FirstLocator*) this;
		return this.infix;
	}
	int FirstLocator_length(void* _this_){
		struct FirstLocator this = *(struct FirstLocator*) this;
		return this.infix.length();
	}
	Optional<Integer> FirstLocator_locate(void* _this_){
		struct FirstLocator this = *(struct FirstLocator*) this;
		temp = temp;
		temp = temp;
	}
};