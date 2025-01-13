#include "temp.h"
struct LastLocator(String slice) implements Locator {
	int computeLength(){
		return temp().length();
	}
	Optional<Integer> locate(String input){
		auto index = input.lastIndexOf(slice());
		if (1) {}
		return Optional.of(index);
	}
};