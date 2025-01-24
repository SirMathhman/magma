#include "../../../../magma/api/stream/Stream.h"
struct Locator{
	String unwrap();
	int length();
	Stream<Integer> locate(String input);
}
