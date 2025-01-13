#include "temp.h"
#include "temp.h"
#include "temp.h"
struct TypeLocator implements Locator {
	char search;
	char enter;
	char exit;
	public TypeLocator(char search, char enter, char exit){
		this.search = from;
		this.enter = from;
		this.exit = from;
	}
	Tuple<Optional<Integer>, Integer> fold(String input, Tuple<Optional<Integer>, Integer> current, int index){
		auto found = current.left();
		if (1) {}
		auto depth = current.right();
		auto c = input.charAt(index);
		if (1) {}
		if (1) {}
		if (1) {}
		return temp();
	}
	int computeLength(){
		return 1;
	}
	Optional<Integer> locate(String input){
		return IntStream.range(0, input.length()).mapToObj(index -> input.length() - 1 - index).reduce(temp(), 0), (current, tuple) -> fold(input, current, tuple), (_, next) -> next).left();
	}
};