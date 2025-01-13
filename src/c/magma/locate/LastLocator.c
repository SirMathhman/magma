#include "temp.h"
struct LastLocator(String slice) implements Locator {
	int computeLength(){
		return slice().length();
	}
	Optional<Integer> locate(String input){
		auto index = input.lastIndexOf(slice());
		if (index == -1) return Optional.empty();
		return Optional.of(index);
	}
};