#include "temp.h"
struct FirstLocator(String slice) implements Locator {
	int computeLength();
	Optional<Integer> locate();
};