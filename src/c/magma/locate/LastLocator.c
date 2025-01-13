#include "temp.h"
struct LastLocator(String slice) implements Locator {
	int computeLength(){
		return slice().length();
	}
	Optional<Integer> locate(String input){
		auto index = input.lastIndexOf(slice());
		();
		return Optional.of(index);
	}
	String createErrorMessage(){
		return "No slice present: '" + this.slice + "'";
	}
};