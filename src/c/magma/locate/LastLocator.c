#include "temp.h"
struct LastLocator(String slice) implements Locator {
	int computeLength();
	Optional<Integer> locate();
};