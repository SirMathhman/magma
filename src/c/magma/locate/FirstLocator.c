#include <temp.h>
struct FirstLocator {
	String infix;
	struct FirstLocator new(String infix){
		struct FirstLocator this;
		this.infix = infix;
		return this;
	}
	String unwrap(){
		return this.infix;
	}
	int length(){
		return this.infix.length();
	}
	Optional<Integer> locate(){
		temp = temp;
		temp = temp;
	}
};