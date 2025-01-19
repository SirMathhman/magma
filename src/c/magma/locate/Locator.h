#include <temp.h>
struct Locator {
	struct Locator new(){
		struct Locator this;
	}
	String unwrap();
	int length();
	Optional<Integer> locate();
};