#include "temp.h"
struct LastLocator(String slice) implements Locator {
	int computeLength(){
		return slice();
	}
	Optional<Integer> locate(String input){
		auto index = input.lastIndexOf();
		if (1) {}
		return Optional.of();
	}
};