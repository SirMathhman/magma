#include "temp.h"
struct FirstLocator(String slice) implements Locator {
	int computeLength(){
		return this.slice.length();
	}
	Optional<Integer> locate(String input){
		auto index = input.indexOf(this.slice);
		if (index == -1) {}
		return Optional.of(index);
	}
};