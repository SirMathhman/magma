#include "temp.h"
struct FirstLocator(String slice) implements Locator {
	int computeLength(){
		return temp;
	}
	Optional<Integer> locate(String input){
		auto index = input.indexOf(this.slice);
		if (1) {}
		return temp;
	}
};